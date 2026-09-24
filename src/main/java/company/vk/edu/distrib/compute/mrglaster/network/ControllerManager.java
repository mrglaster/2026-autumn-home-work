package company.vk.edu.distrib.compute.mrglaster.network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.mrglaster.annotation.Route;
import company.vk.edu.distrib.compute.mrglaster.service.AuthorizationService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerManager {

    private final List<RouteDefinition> routes = new ArrayList<>();
    private final AuthorizationService authService;

    public ControllerManager(AuthorizationService authService) {
        this.authService = authService;
    }

    private static String[] splitPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return new String[0];
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return new String[0];
        }
        return path.split("/");
    }

    public void addController(Object controller) {
        Class<?> clazz = controller.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Route.class)) {
                Route route = method.getAnnotation(Route.class);
                routes.add(new RouteDefinition(route.method().toUpperCase(), route.path(), route.requiresAuthorization(), method, controller));
            }
        }
    }

    public void register(HttpServer server) {
        server.createContext("/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod().toUpperCase();

            RouteMatch match = findMatchingRoute(requestMethod, requestPath);

            if (match != null) {
                if (match.route.requiresAuthorization) {
                    try {
                        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                        if (!authService.checkBasicAuth(authHeader)) {
                            exchange.sendResponseHeaders(401, -1);
                            exchange.close();
                            return;
                        }
                    } catch (Exception e) {
                        exchange.sendResponseHeaders(401, -1);
                        exchange.close();
                        return;
                    }
                }
                invokeHandler(match, exchange);
            } else {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
            }
        });
    }

    private RouteMatch findMatchingRoute(String method, String path) {
        String[] pathSegments = splitPath(path);

        for (RouteDefinition route : routes) {
            if (!route.httpMethod.equals(method)) continue;
            if (route.pathSegments.length != pathSegments.length) continue;

            Map<String, String> extractedParams = new HashMap<>();
            boolean isMatch = true;

            for (int i = 0; i < route.pathSegments.length; i++) {
                String routeSegment = route.pathSegments[i];
                String pathSegment = pathSegments[i];

                if (routeSegment.startsWith("{") && routeSegment.endsWith("}")) {
                    String paramName = routeSegment.substring(1, routeSegment.length() - 1);
                    extractedParams.put(paramName, pathSegment);
                } else if (!routeSegment.equals(pathSegment)) {
                    isMatch = false;
                    break;
                }
            }

            if (isMatch) {
                return new RouteMatch(route, extractedParams);
            }
        }
        return null;
    }

    private void invokeHandler(RouteMatch match, HttpExchange exchange) {
        Method method = match.route.method;
        Object instance = match.route.instance;
        method.setAccessible(true);

        try {
            Object[] args = new Object[method.getParameterCount()];
            for (int i = 0; i < method.getParameterCount(); i++) {
                Class<?> paramType = method.getParameterTypes()[i];
                if (paramType == HttpExchange.class) {
                    args[i] = exchange;
                } else if (paramType == Map.class) {
                    args[i] = match.pathParams;
                } else {
                    throw new UnsupportedOperationException("Неподдерживаемый тип параметра: " + paramType);
                }
            }

            method.invoke(instance, args);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (exchange.getResponseCode() == -1) {
                    exchange.sendResponseHeaders(500, -1);
                }
            } catch (IOException ignored) {
            }
        } finally {
            if (exchange.getResponseCode() == -1) {
                try {
                    exchange.sendResponseHeaders(200, -1);
                } catch (IOException ignored) {
                }
            }
            exchange.close();
        }
    }

    private static class RouteDefinition {
        final String httpMethod;
        final String[] pathSegments;
        final boolean requiresAuthorization;
        final Method method;
        final Object instance;

        RouteDefinition(String httpMethod, String path, boolean requiresAuthorization, Method method, Object instance) {
            this.httpMethod = httpMethod;
            this.requiresAuthorization = requiresAuthorization;
            this.method = method;
            this.instance = instance;
            this.pathSegments = splitPath(path);
        }
    }

    private record RouteMatch(RouteDefinition route, Map<String, String> pathParams) {
    }
}
package company.vk.edu.distrib.compute.mrglaster.controller.external;

import company.vk.edu.distrib.compute.mrglaster.annotation.Route;
import company.vk.edu.distrib.compute.mrglaster.controller.model.BaseController;

public class StatusController extends BaseController {
    @Route(method = "GET", path = "/v0/status", requiresAuthorization = false)
    public String getStatus() {
        return "UP";
    }
}

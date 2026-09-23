package company.vk.edu.distrib.compute.mrglaster.controller;

import company.vk.edu.distrib.compute.mrglaster.annotation.Route;

public class StatusController {
    @Route(method = "GET", path = "/v0/status")
    public String getStatus() {
        return "UP";
    }
}

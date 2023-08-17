package com.leilei.vtubersupporter.cotroller;

import com.leilei.vtubersupporter.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author leifengsang
 */
@RestController
@CrossOrigin
public class ActListener {

    @Autowired
    private ApiService apiService;

    @RequestMapping("/api/dead")
    public String onDead() {
        apiService.onDead();
        return "succ";
    }

    @RequestMapping("/api/deadExpired")
    public String onDeadExpired() {
        apiService.onDeadExpired();
        return "succ";
    }

    @RequestMapping("/api/damageDown")
    public String onDamageDown() {
        apiService.onDamageDown();
        return "succ";
    }

    @RequestMapping("api/damageDownExpired")
    public String onDamageDownExpired() {
        apiService.onDamageDownExpired();
        return "succ";
    }

    @RequestMapping("/api/weakness")
    public String onWeakness() {
        apiService.onWeakness();
        return "succ";
    }

    @RequestMapping("api/weaknessExpired")
    public String onWeaknessExpired() {
        apiService.onWeaknessExpired();
        return "succ";
    }

    @RequestMapping("api/reset")
    public String onReset() {
        apiService.onReset();
        return "succ";
    }
}

package com.leilei.vtubersupporter.cotroller;

import com.leilei.vtubersupporter.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author leifengsang
 */
@Controller
public class ActListener {

    @Autowired
    private ApiService apiService;

    @RequestMapping("/api/dead")
    public void onDead() {
        apiService.onDead();
    }

    @RequestMapping("/api/deadExpired")
    public void onDeadExpired() {
        apiService.onDeadExpired();
    }

    @RequestMapping("/api/damageDown")
    public void onDamageDown() {
        apiService.onDamageDown();
    }

    @RequestMapping("api/damageDownExpired")
    public void onDamageDownExpired() {
        apiService.onDamageDownExpired();
    }

    @RequestMapping("/api/weakness")
    public void onWeakness() {
        apiService.onWeakness();
    }

    @RequestMapping("api/weaknessExpired")
    public void onWeaknessExpired() {
        apiService.onWeaknessExpired();
    }
}

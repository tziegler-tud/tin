package tinCORE.controller.v1

import org.springframework.web.bind.annotation.*
import tinCORE.data.api.settings.ApiSettingsData
import tinCORE.data.api.settings.SettingsInfoData
import tinCORE.services.Task.TaskService

@RestController
@RequestMapping("/api/v1/settings")
class SettingsController(
) {

    @GetMapping("/all")
    fun getSettings(): SettingsInfoData {

        return SettingsInfoData();
    }

    @PostMapping("/set")
    fun setSettings(@RequestBody data: ApiSettingsData): SettingsInfoData {
        return SettingsInfoData();
    }
}
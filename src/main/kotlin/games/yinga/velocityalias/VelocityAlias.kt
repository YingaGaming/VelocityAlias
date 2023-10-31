package games.yinga.velocityalias

import com.google.inject.Inject
import com.moandjiezana.toml.Toml
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import org.slf4j.Logger

@Plugin(
        id = "velocityalias",
        name = "VelocityAlias",
        version = "1.0.0",
        description = "Command aliases for Velocity",
        authors = ["xenorio"]
)
class VelocityAlias @Inject constructor(val proxy: ProxyServer, val logger: Logger) {

    val configPath = "./plugins/VelocityAlias/config.toml"

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {

        event.toString() // Do something with the event to not produce warnings

		// Load config
        var configPathParsed = Path.of(configPath)
        if (!Files.exists(configPathParsed)) {
            var configTemplate = javaClass.getResourceAsStream("/config.toml")
            Files.createDirectories(configPathParsed.getParent())
            Files.write(configPathParsed, configTemplate.readAllBytes(), StandardOpenOption.CREATE_NEW)
        }

        val config = Toml().read(File(configPath))

        // Register command aliases from the configuration
        val aliasesSection = config.getTable("aliases")
        val aliasesMap = aliasesSection.toMap()
        for ((aliasKey, originalCommand) in aliasesMap) {
            logger.info("Registering '" + aliasKey + "' as '" + originalCommand + "'")
            proxy.commandManager.register(
                    aliasKey,
                    object : SimpleCommand {
                        override fun execute(invocation: SimpleCommand.Invocation) {
                            proxy.commandManager.executeAsync(
                                    invocation.source(),
                                    originalCommand as String
                            )
                        }
                    }
            )
        }

        logger.info("VelocityAlias Enabled")
    }
}

package com.example.botexpo;

import net.dv8tion.jda.api.JDA;

import net.dv8tion.jda.api.JDABuilder;

import net.dv8tion.jda.api.requests.GatewayIntent;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;



@SpringBootApplication

public class DiscordTravelBotApplication {



	@Value("${discord.bot.token:}")

	private String discordToken;



	public static void main(String[] args) {

		SpringApplication.run(DiscordTravelBotApplication.class, args);

	}



	@Bean

	public JDA jda(DiscordTravelBot travelBot) throws Exception {

		System.out.println("DEBUG: Leyendo token...");

		System.out.println("DEBUG: Token es null? " + (discordToken == null));

		System.out.println("DEBUG: Token está vacío? " + (discordToken != null && discordToken.isEmpty()));

		System.out.println("DEBUG: Token length: " + (discordToken != null ? discordToken.length() : 0));



		if (discordToken == null || discordToken.isEmpty()) {

			System.err.println("❌ ERROR: discord.bot.token no está configurado en application.properties");

			System.err.println("❌ Verifica que DISCORD_TOKEN esté en las variables de entorno de Render");

			throw new RuntimeException("discord.bot.token es requerido");

		}



		System.out.println("✅ Token leído correctamente, iniciando bot...");



		JDA jda = JDABuilder.createDefault(discordToken,

						GatewayIntent.DIRECT_MESSAGES,

						GatewayIntent.MESSAGE_CONTENT,

						GatewayIntent.GUILD_MESSAGES)

				.addEventListeners(travelBot)

				.build();

		jda.awaitReady();

		System.out.println("✅ Bot iniciado correctamente");

		return jda;

	}

}
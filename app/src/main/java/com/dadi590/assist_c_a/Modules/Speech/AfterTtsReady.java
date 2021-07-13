package com.dadi590.assist_c_a.Modules.Speech;

import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

/**
 * <p>Same as in {@link #afterTtsReady()}.</p>
 */
final class AfterTtsReady {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private AfterTtsReady() {
	}

	/**
	 * <p>List of important things to do right after TTS is ready.</p>
	 */
	static void afterTtsReady() {
		//UtilsGeneral.checkWarnRootAccess(false); Not supposed to be needed root access. Only system permissions.

		switch (UtilsApp.appInstallationType()) {
			case UtilsApp.SYSTEM_WITHOUT_UPDATES: {
				final String speak = "WARNING - Installed as system application but without updates. Only " +
						"emergency code commands will be available.";
				// todo Is it so? Even on Marshmallow and above with extractNativeLibs=false...? Test that.
				//  Remember the user who said you could "potentially" emulate loading from the APK itself? Try that
				//  below Marshmallow... Maybe read the APK? Or extract it to memory and load from memory? (always from
				//  memory, preferably)
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
				break;
			}
			case UtilsApp.NORMAL: {
				final String speak = "WARNING - Installed as normal application! System features may not be " +
						"available.";
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
				break;
			}
		}

		if (!UtilsApp.isDeviceAdmin()) {
			final String speak = "WARNING - The application is not a Device Administrator! Some security " +
					"features may not be available.";
			MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
		}

		/*if (app_installation_type == UtilsApp.SYSTEM_WITHOUT_UPDATES) {
			switch (Copiar_bibliotecas.copiar_biblioteca_PocketSphinx(getApplicationContext())) {
				case ARQUITETURA_NAO_DISPONIVEL: {
					// Não é preciso ser fala de emergência, já que isto é das primeiras coisa que ele diz.
					pocketsphinx_disponivel = false;
					fala.speak("WARNING - It was not possible to find a compatible CPU architecture for PocketSphinx " +
							"library to be copied to the device. It will not be possible to have background hotword " +
							"detection.", Fala.SEM_COMANDOS_ADICIONAIS, null, false);
					break;
				}
				case ERRO_COPIA: {
					// Não é preciso ser fala de emergência, já que isto é das primeiras coisa que ele diz.
					pocketsphinx_disponivel = false;
					fala.speak("WARNING - It was not possible to copy the PocketSphinx library to the device. It will " +
							"not be possible to have background hotword detection.", Fala.SEM_COMANDOS_ADICIONAIS,
							null, false);
					break;
				}
			}
		}*/

		//Utils_reconhecimentos_voz.iniciar_reconhecimento_pocketsphinx();

		//pressao_longa_botoes.ativar_detecao(Build.VERSION.SDK_INT);

		MainSrv.getMainRegBroadcastRecv().registerReceivers();

		// The Main Service is completely ready, so it warns about it so we can start speaking to it (very useful in
		// case the screen gets broken, for example).
		// It's also said in top priority so the user can know immediately (hopefully) that the assistant is ready.
		final String speak = "Ready, sir.";
		MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
	}
}

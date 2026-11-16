package com.deary.server.domain.push.service;

import com.deary.server.domain.push.entity.PushLanguage;

/**
 * 알림 종류별 제목·본문. {name}은 상대 닉네임이며, 닉네임이 없으면 이름 없는 문구를 쓴다.
 */
enum PushMessage {

	CORRECTION_COMPLETED(
			new Text("교정이 도착했어요", "{name} 님이 내 일기를 고쳐 줬어요. 지금 확인해 보세요.", "누군가 내 일기를 고쳐 줬어요. 지금 확인해 보세요."),
			new Text("添削が届きました", "{name}さんがあなたの日記を添削してくれました。さっそく見てみましょう。", "あなたの日記に添削が届きました。さっそく見てみましょう。"),
			new Text("Your correction has arrived", "{name} corrected your diary. Take a look!", "Someone corrected your diary. Take a look!")),
	CORRECTION_THANKED(
			new Text("고마워요가 도착했어요", "{name} 님이 내 교정에 고마워요를 보냈어요.", "내 교정에 고마워요가 도착했어요."),
			new Text("ありがとうが届きました", "{name}さんがあなたの添削にありがとうを送りました。", "あなたの添削にありがとうが届きました。"),
			new Text("You got a thank-you", "{name} sent thanks for your correction.", "Someone sent thanks for your correction."));

	private final Text ko;
	private final Text ja;
	private final Text en;

	PushMessage(Text ko, Text ja, Text en) {
		this.ko = ko;
		this.ja = ja;
		this.en = en;
	}

	String title(PushLanguage language) {
		return text(language).title();
	}

	String body(PushLanguage language, String name) {
		Text text = text(language);
		return name == null || name.isBlank() ? text.anonymousBody() : text.body().replace("{name}", name);
	}

	private Text text(PushLanguage language) {
		return switch (language) {
			case KO -> ko;
			case JA -> ja;
			case EN -> en;
		};
	}

	private record Text(String title, String body, String anonymousBody) {
	}

}

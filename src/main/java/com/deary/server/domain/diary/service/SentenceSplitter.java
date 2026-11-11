package com.deary.server.domain.diary.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SentenceSplitter {

	private static final String LATIN_TERMINATORS = ".!?";
	private static final String CJK_TERMINATORS = "。！？";
	private static final String CLOSERS = "\"'”’)」』】）";

	public List<String> split(String text) {
		List<String> sentences = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		int length = text.length();
		for (int i = 0; i < length; i++) {
			char c = text.charAt(i);
			if (c == '\n' || c == '\r') {
				flush(current, sentences);
				continue;
			}
			current.append(c);
			if (isTerminator(c) && isSentenceEnd(text, i)) {
				while (i + 1 < length && (isTerminator(text.charAt(i + 1)) || CLOSERS.indexOf(text.charAt(i + 1)) >= 0)) {
					current.append(text.charAt(++i));
				}
				flush(current, sentences);
			}
		}
		flush(current, sentences);
		return sentences;
	}

	private boolean isTerminator(char c) {
		return LATIN_TERMINATORS.indexOf(c) >= 0 || CJK_TERMINATORS.indexOf(c) >= 0;
	}

	private boolean isSentenceEnd(String text, int index) {
		char c = text.charAt(index);
		if (CJK_TERMINATORS.indexOf(c) >= 0) {
			return true;
		}
		int next = index + 1;
		while (next < text.length() && (isTerminator(text.charAt(next)) || CLOSERS.indexOf(text.charAt(next)) >= 0)) {
			next++;
		}
		return next >= text.length() || Character.isWhitespace(text.charAt(next));
	}

	private void flush(StringBuilder current, List<String> sentences) {
		String sentence = current.toString().strip();
		if (!sentence.isEmpty()) {
			sentences.add(sentence);
		}
		current.setLength(0);
	}

}

/*
 * Copyright (C) 2026 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.ai.tool;

import java.util.Collections;
import java.util.List;

/**
 * A single AI tool menu entry for the HTML editor. Leaves carry a prompt
 * key (and optionally an input spec / selection requirement); parents carry
 * a non-empty children list.
 *
 * Prompts, labels, and no-selection error messages are referenced by locale
 * keys and resolved at service-vars build time.
 */
public final class AIToolItem {

	private final String id;
	private final String labelKey;
	private final String promptKey;
	private final AIToolInputSpec input;
	private final boolean requiresSelection;
	private final String noSelectionErrorKey;
	private final List<AIToolItem> children;

	public AIToolItem(
			String id,
			String labelKey,
			String promptKey,
			AIToolInputSpec input,
			boolean requiresSelection,
			String noSelectionErrorKey,
			List<AIToolItem> children) {
		this.id = id;
		this.labelKey = labelKey;
		this.promptKey = promptKey;
		this.input = input;
		this.requiresSelection = requiresSelection;
		this.noSelectionErrorKey = noSelectionErrorKey;
		this.children = children == null
				? Collections.<AIToolItem>emptyList()
				: Collections.unmodifiableList(children);
	}

	public String getId() { return id; }
	public String getLabelKey() { return labelKey; }
	public String getPromptKey() { return promptKey; }
	public AIToolInputSpec getInput() { return input; }
	public boolean requiresSelection() { return requiresSelection; }
	public String getNoSelectionErrorKey() { return noSelectionErrorKey; }
	public List<AIToolItem> getChildren() { return children; }

	public boolean isGroup() { return !children.isEmpty(); }
	public boolean isLeaf() { return children.isEmpty(); }
}

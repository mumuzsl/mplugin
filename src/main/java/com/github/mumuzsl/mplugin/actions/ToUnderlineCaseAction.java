package com.github.mumuzsl.mplugin.actions;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 代码copy from<a href="https://github.com/JetBrains/intellij-sdk-code-samples/blob/main/editor_basics/src/main/java/org/intellij/sdk/editor/EditorIllustrationAction.java">官方示例</a>
 *
 * @auther zhaosenlin
 * @date 2022/9/4 18:10
 */
public class ToUnderlineCaseAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Get required data keys
        final Project project = e.getProject();
        final Editor  editor  = e.getData(CommonDataKeys.EDITOR);
        // Set visibility and enable only in case of existing project and editor and if a selection exists
        e.getPresentation().setEnabledAndVisible(
                project != null && editor != null && editor.getSelectionModel().hasSelection()
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Get all the required data from data keys
        // Editor and Project were verified in update(), so they are not null.
        // Editor和Project已经在update()得到验证，这里它们不会为null
        final Editor   editor   = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project  project  = e.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        // Work off of the primary caret to get the selection info
        Caret  primaryCaret = editor.getCaretModel().getPrimaryCaret();
        String selectedText = primaryCaret.getSelectedText();
        int    start        = primaryCaret.getSelectionStart();
        int    end          = primaryCaret.getSelectionEnd();
        String newStr       = StrUtil.nullToEmpty(StrUtil.toUnderlineCase(selectedText));
        System.out.println(newStr);
        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(start, end, newStr)
        );
        // De-select the text range that was just replaced
        primaryCaret.removeSelection();
    }
}

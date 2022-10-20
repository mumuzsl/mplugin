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

import java.sql.Struct;

/**
 * @auther zhaosenlin
 * @date 2022/10/20 21:23
 */
public class ToUpperOrLowerAction extends AnAction {
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
        // Editor和Project已经在update()得到验证，这里它们不会为null
        final Editor   editor   = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project  project  = e.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();

        Caret  primaryCaret = editor.getCaretModel().getPrimaryCaret();
        String selectedText = primaryCaret.getSelectedText();
        if (selectedText == null || selectedText.length() == 0) {
            return;
        }

        int start = primaryCaret.getSelectionStart();
        int end   = primaryCaret.getSelectionEnd();

        char[] chars = selectedText.toCharArray();
        char   first = chars[0];
        if (first >= 'A' && first <= 'Z') {
            chars[0] = (char) (first + 32);
        } else if (first >= 'a' && first <= 'z') {
            chars[0] = (char) (first - 32);
        }
        String newStr = new String(chars);
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(start, end, newStr)
        );
        primaryCaret.removeSelection();
    }
}

package com.github.mumuzsl.mplugin.actions;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @auther zhaosenlin
 * @date 2022/9/5 19:26
 */
public class JsonStrToMapAction extends AnAction {

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
        String newStr       = convert(selectedText);
        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(start, end, newStr)
        );
        // De-select the text range that was just replaced
        primaryCaret.removeSelection();
    }

    private static String convert(String selectedText) {
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        StringBuilder sb = new StringBuilder(selectedText).append("\n");
        sb.append("Map<String, Object> map = new HashMap<String, Object>()\n");
        try {
            Map<String, Object> map = new ObjectMapper().readValue(selectedText, typeReference);
            map.forEach((k, v) -> {
                String line = StrUtil.format("map.put(\"{}\", {});\n", k, v);
                sb.append(line);
            });
            return sb.toString();
        } catch (Exception ex) {
            return sb.append(ex.getMessage()).append("\n").toString();
        }
    }

}

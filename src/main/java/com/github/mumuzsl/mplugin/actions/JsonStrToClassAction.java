package com.github.mumuzsl.mplugin.actions;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @auther zhaosenlin
 * @date 2022/9/5 19:56
 */
public class JsonStrToClassAction extends AnAction {

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

    public static String convert(String selectedText) {
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        StringBuilder sb = new StringBuilder(selectedText).append("\n");
        try {
            Map<String, Object> map = new ObjectMapper().readValue(selectedText, typeReference);
            handleMap(sb, "json_to_class", map);
            return sb.toString();
        } catch (Exception ex) {
            return sb.append(ex.getMessage()).append("\n").toString();
        }
    }

    public static void handleList(StringBuilder sb, String key, List<?> list) {
        if (!list.isEmpty()) {
            Object o = list.get(0);
            if (o instanceof Map) {
                String className = buildClassName(key);
                sb.append(StrUtil.format("private List<{}> {}List;\n", className, StrUtil.lowerFirst(className)));
                getClassStr(sb, className, (Map<?, ?>) o);
            } else if (o instanceof List) {
                handleList(sb, key, (List<?>) o);
            } else {
                sb.append(StrUtil.format("private List<{}> {}List;\n", o.getClass().getSimpleName(), buildFieldName(key)));
            }
        } else {
            sb.append(StrUtil.format("private List<?> {}List;\n", buildFieldName(key)));
        }
    }

    public static void handleMap(StringBuilder sb, String key, Map<?, ?> map) {
        String className = buildClassName(key);
        sb.append(StrUtil.format("private {} {};\n", className, StrUtil.lowerFirst(className)));

        getClassStr(sb, className, map);
    }

    public static void getClassStr(StringBuilder sb, String className, Map<?, ?> map) {
        try {
            sb.append("@Data\npublic static class ").append(className).append(" {\n");
            map.forEach((k, v) -> {
                String key = String.valueOf(k);
                sb.append("@JsonProperty(\"").append(key).append("\")\n");
                if (v instanceof Map) {
                    handleMap(sb, key, (Map<?, ?>) v);
                } else if (v instanceof List) {
                    handleList(sb, key, (List<?>) v);
                } else {
                    sb.append(StrUtil.format("private {} {};\n", v.getClass().getSimpleName(), StrUtil.toCamelCase(key)));
                }
            });
            sb.append("}\n");
        } catch (Exception ex) {
            sb.append(ex.getMessage()).append("\n");
        }
    }

    public static String buildFieldName(String key) {
        return StrUtil.lowerFirst(StrUtil.toCamelCase(key));
    }

    public static String buildClassName(String key) {
        return StrUtil.upperFirst(StrUtil.toCamelCase(key));
    }

}

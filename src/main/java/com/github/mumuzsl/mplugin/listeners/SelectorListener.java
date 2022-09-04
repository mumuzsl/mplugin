package com.github.mumuzsl.mplugin.listeners;

import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import org.jetbrains.annotations.NotNull;

/**
 * @auther zhaosenlin
 * @date 2022/9/4 17:50
 */
public class SelectorListener implements SelectionListener {

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        SelectionListener.super.selectionChanged(e);
    }
}

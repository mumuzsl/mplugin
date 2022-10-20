package com.github.mumuzsl.mplugin.entity;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther zhaosenlin
 * @date 2022/9/24 20:51
 */
public class UploadInfo {
    private SshInfo sshInfo;

    private String dest;

    private List<VirtualFile> virtualFiles;

    public UploadInfo() {
        this.sshInfo = new SshInfo();
    }

    public UploadInfo(SshInfo sshInfo) {
        this.sshInfo = sshInfo;
    }

    public void setSshInfo(SshInfo sshInfo) {
        this.sshInfo = sshInfo;
    }

    public SshInfo getSshInfo() {
        return sshInfo;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public List<VirtualFile> getVirtualFiles() {
        return virtualFiles;
    }

    public void setVirtualFiles(List<VirtualFile> virtualFiles) {
        this.virtualFiles = virtualFiles;
    }
}

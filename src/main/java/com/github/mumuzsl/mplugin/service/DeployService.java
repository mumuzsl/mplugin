package com.github.mumuzsl.mplugin.service;

import com.github.mumuzsl.mplugin.actions.DeployAction;
import com.github.mumuzsl.mplugin.entity.SshInfo;
import com.github.mumuzsl.mplugin.entity.UploadInfo;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.TransferListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @auther zhaosenlin
 * @date 2022/9/24 20:49
 */
@State(name = "DeployService")
public class DeployService implements PersistentStateComponent<UploadInfo> {

    public static DeployService getInstance() {
        DeployService deployService = new DeployService();

        final SshInfo    sshInfo    = new SshInfo();
        final UploadInfo uploadInfo = new UploadInfo(sshInfo);
        uploadInfo.setVirtualFiles(new ArrayList<>());
        deployService.setUploadInfo(uploadInfo);

        return deployService;
    }

    private final AtomicLong uploadPress = new AtomicLong(0);
    private UploadInfo uploadInfo;

    public void setUploadInfo(UploadInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
    }

    @Override
    public @Nullable UploadInfo getState() {
        return uploadInfo;
    }

    @Override
    public void loadState(@NotNull UploadInfo state) {
        this.uploadInfo = state;
    }

    public AtomicLong getUploadPress() {
        return uploadPress;
    }

    public void upload() {
        upload(uploadInfo);
    }

    public void upload(final UploadInfo info) {
        setUploadInfo(info);
        try (SSHClient ssh = new SSHClient()) {
            ssh.loadKnownHosts();
            // 跳过密钥验证
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            SshInfo sshInfo = info.getSshInfo();
            ssh.connect(sshInfo.getIp());
            ssh.authPassword(sshInfo.getUsername(), sshInfo.getPassword());
            try (SFTPClient sftp = ssh.newSFTPClient()) {
                TransferListener      transferListener      = sftp.getFileTransfer().getTransferListener();
                UploadProcessListener uploadProcessListener = new UploadProcessListener(transferListener);
                sftp.getFileTransfer().setTransferListener(uploadProcessListener);
                for (VirtualFile virtualFile : info.getVirtualFiles()) {
                    sftp.put(virtualFile.getPath(), info.getDest());
                }
            }
        } catch (IOException e) {
            uploadPress.set(-1);
            e.printStackTrace();
        }
    }

    public class UploadProcessListener implements TransferListener {
        TransferListener transferListener;

        public UploadProcessListener(TransferListener transferListener) {
            this.transferListener = transferListener;
        }

        @Override
        public TransferListener directory(String name) {
            return transferListener.directory(name);
        }

        @Override
        public StreamCopier.Listener file(String name, long size) {
            long curSize = uploadPress.addAndGet(size);
            System.out.println(curSize);
            return transferListener.file(name, size);
        }

    }
}

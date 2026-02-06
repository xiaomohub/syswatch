package org.xiaomo.syswatch.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.annotation.AlertLog;
import org.xiaomo.syswatch.config.PrometheusProperties;
import org.xiaomo.syswatch.service.SshFileService;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class SshFileServiceImpl implements SshFileService {

    @Resource
    private PrometheusProperties prometheusProperties;

    @Resource
    private org.xiaomo.syswatch.config.SshProperties sshProperties;

    @Override
    @AlertLog(action="ssh远程写入文件")
    public void writeRuleFile(String fileName, String content) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            session = jsch.getSession(
                    sshProperties.getUsername(),
                    sshProperties.getHost(),
                    sshProperties.getPort()
            );
            session.setPassword(sshProperties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);

            String remoteFile = prometheusProperties.getRuleDir() + fileName;
            String command = "echo " + escapeForBash(content) + " > " + remoteFile;

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            // 等待执行完成
            while (!channel.isClosed()) {
                Thread.sleep(50);
            }

        } catch (Exception e) {
            throw new RuntimeException("写 Prometheus 规则文件失败: " + fileName, e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    // 对内容做简单转义，避免特殊字符破坏 bash 命令
    private String escapeForBash(String content) {
        return "'" + content.replace("'", "'\"'\"'") + "'";
    }


    @Override
    @AlertLog(action="ssh校验哈希是否相同")
    public String getRemoteSha256(String fileName) {

        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            session = jsch.getSession(
                    sshProperties.getUsername(),
                    sshProperties.getHost(),
                    sshProperties.getPort()
            );
            session.setPassword(sshProperties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);

            String remoteFile = prometheusProperties.getRuleDir() + fileName;
            String command = "sha256sum " + remoteFile + " | awk '{print $1}'";

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            channel.connect();

            String result = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();

            if (result.isEmpty()) {
                throw new IllegalStateException("远程 hash 为空：" + remoteFile);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("获取远程规则文件 hash 失败: " + fileName, e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

}

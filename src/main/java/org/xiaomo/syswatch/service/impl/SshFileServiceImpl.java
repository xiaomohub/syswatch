package org.xiaomo.syswatch.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.annotation.AlertLog;
import org.xiaomo.syswatch.config.PrometheusProperties;
import org.xiaomo.syswatch.service.SshFileService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class SshFileServiceImpl implements SshFileService {

    @Resource
    private PrometheusProperties prometheusProperties;

    @Resource
    private org.xiaomo.syswatch.config.SshProperties sshProperties;

    @Override
    @AlertLog(action = "ssh远程写入规则文件")
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

    @Override
    @AlertLog(action = "ssh远程读取规则文件")
    public String readRuleFile(String fileName) {
        String remoteFile = prometheusProperties.getRuleDir() + fileName;
        String command = "cat " + remoteFile;
        return sshExecute(command);
    }

    /**
     * 统一的 SSH 执行方法（读文件 / 校验 / 其他命令都能复用）
     */
    private String sshExecute(String command) {
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

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            channel.connect();

            StringBuilder result = new StringBuilder();
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }

            while (!channel.isClosed()) {
                Thread.sleep(50);
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException("SSH 执行失败: " + command, e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    /**
     * bash 安全转义
     */
    private String escapeForBash(String content) {
        return "'" + content.replace("'", "'\"'\"'") + "'";
    }
}

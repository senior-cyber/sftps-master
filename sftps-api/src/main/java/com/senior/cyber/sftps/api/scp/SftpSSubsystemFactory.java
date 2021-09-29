package com.senior.cyber.sftps.api.scp;

import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;

public class SftpSSubsystemFactory extends org.apache.sshd.sftp.server.SftpSubsystemFactory {

    public SftpSSubsystemFactory() {
    }

    @Override
    public Command createSubsystem(ChannelSession channel) throws IOException {
        SftpSSubsystem subsystem = new SftpSSubsystem(channel, this);
        GenericUtils.forEach(getRegisteredListeners(), subsystem::addSftpEventListener);
        return subsystem;
    }
}

package org.lavocat.PluginExemple.tcp.commands;

import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerBuilder;

public class ExempleCommand extends AbstractCommand {

    @Override
    public AMPAnswerBuilder execute(String answer, String hash) {
        // TODO Auto-generated method stub
        return new AMPAnswerBuilder(answer, null);
    }

}

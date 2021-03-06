/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.plugins.PluginExemple.webservice.commands;

import java.util.Iterator;
import java.util.Map;
import org.meta.api.ws.AbstractPluginWebServiceController;
import org.meta.api.ws.AbstractWebService;
import org.meta.api.ws.forms.fields.DateInput;
import org.meta.api.ws.forms.fields.TextOutput;
import org.meta.api.ws.forms.submit.SelfSubmitButton;
import org.meta.api.ws.forms.submit.SubmitToButton;

/**
 * <p>ExempleWsCommand class.</p>
 *
 * @author nico
 * @version $Id: $Id
 */
public class ExempleWsCommand extends AbstractWebService {

    private TextOutput output = null;
    private int nbRefresh = 0;
    private DateInput birthDate = null;

    /**
     * <p>Constructor for ExempleWsCommand.</p>
     *
     * @param controler a {@link org.meta.api.ws.AbstractPluginWebServiceController} object.
     */
    public ExempleWsCommand(AbstractPluginWebServiceController controler) {
        super(controler);
        // Describe a full interface for test
        birthDate = new DateInput("birthDate", "Birth Date");
        rootColumn.addChild(birthDate);
        rootColumn.addChild(new SelfSubmitButton("submit", "submit form to me"));
        rootColumn.addChild(
                new SubmitToButton(
                        "submitTo",
                        "submit to secondExample",
                        "secondExample"));
        output = new TextOutput("output", "Sortie");
        output.append("message");
        rootColumn.addChild(output);
    }

    /** {@inheritDoc} */
    @Override
    public void executeCommand(Map<String, String[]> map) {
        output.flush();
        birthDate.setValue(getParameter(birthDate.getId(), map));
        output.append("Your sended parameters are :");
        for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            output.append(" - " + key + " : " + map.get(key));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void applySmallUpdate() {
        nbRefresh++;
        output.append("refresh number" + nbRefresh);
        birthDate.setValue("tututu");
    }
}

package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/24/2018
 */
public class StubComponentAction implements EditActionInterface {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(final String id, final String value, final EditHolder holder) {

        if (StringUtils.isNotBlank(id) && Constants.ID_PATTERN.matcher(id).matches()) {

            for (ComponentDefinition componentDefinition : holder.getViewDefinition().getComponents()) {
                if (componentDefinition.getComponentId().equals(id)) {
                    logger.info("Component with same ID already exists");
                    return "Component with same ID already exists";
                }
            }

            ComponentDefinition componentDefinition = new ComponentDefinition();
            componentDefinition.setComponentId(id);
            componentDefinition.setName(id);

            componentDefinition.setX(0);
            componentDefinition.setY(0);
            componentDefinition.setW(1);
            componentDefinition.setH(1);
            componentDefinition.setEnabled(false);

            holder.getViewDefinition().getComponents().add(componentDefinition);

            holder.getViewDefinition().sort();
            holder.getViewDefinition().save();

            return "";
        } else {
            return "Component ID can only contain (a-z A-Z 0-9 - _)";
        }
    }
}

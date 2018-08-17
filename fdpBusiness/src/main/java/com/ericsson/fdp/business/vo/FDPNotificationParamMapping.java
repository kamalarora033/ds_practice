package com.ericsson.fdp.business.vo;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;
import com.ericsson.fdp.dao.enums.CommandParameterType;

public class FDPNotificationParamMapping implements FDPCacheable {

                /**
                * 
                 */
                private static final long serialVersionUID = 1L;

                /** The template id. */
                private Long notificationParamId;

                /** The template name. */
                private String paramName;

                /** The template disp name. */
                private String paramDisplay;

                /** The feed type. */
                private ParameterFeedType feedType;

                /** The primitive type. */
                private Primitives primitiveType;

                /** The type. */
                private CommandParameterType xmlType;

                /** The type gui. */
                private CommandParameterDataType typeGUI;

                /** The value. */
                private String paramValue;

                public Long getNotificationParamId() {
                                return notificationParamId;
                }

                public void setNotificationParamId(Long notificationParamId) {
                                this.notificationParamId = notificationParamId;
                }

                public String getParamName() {
                                return paramName;
                }

                public void setParamName(String paramName) {
                                this.paramName = paramName;
                }

                public String getParamDisplay() {
                                return paramDisplay;
                }

                public void setParamDisplay(String paramDisplay) {
                                this.paramDisplay = paramDisplay;
                }

                public ParameterFeedType getFeedType() {
                                return feedType;
                }

                public void setFeedType(ParameterFeedType feedType) {
                                this.feedType = feedType;
                }

                public Primitives getPrimitiveType() {
                                return primitiveType;
                }

                public void setPrimitiveType(Primitives primitiveType) {
                                this.primitiveType = primitiveType;
                }

                public CommandParameterType getXmlType() {
                                return xmlType;
                }

                public void setXmlType(CommandParameterType xmlType) {
                                this.xmlType = xmlType;
                }

                public CommandParameterDataType getTypeGUI() {
                                return typeGUI;
                }

                public void setTypeGUI(CommandParameterDataType typeGUI) {
                                this.typeGUI = typeGUI;
                }

                public String getParamValue() {
                                return paramValue;
                }

                public void setParamValue(String paramValue) {
                                this.paramValue = paramValue;
                }
                
                
}

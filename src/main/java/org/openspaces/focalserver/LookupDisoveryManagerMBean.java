/*******************************************************************************
 * Copyright (c) 2006-2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.focalserver;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Manage mbean Jini discovery settings
 */
public interface LookupDisoveryManagerMBean {

    /**
     * list discovery groups
     *
     * @return
     */
    public String[] getGroups();

    /**
     * add a discovery group
     *
     * @param group
     */
    public void addGroup(String group) throws IOException;

    public void setGroups(String[] groups) throws IOException;

    /**
     * remove discovery group
     *
     * @param group
     */
    public void removeGroup(String group);

    /**
     * set discovery to ALL_GROUPS
     */
    public void setAllGroups() throws IOException;

    /**
     * list of unicast urls
     *
     * @return
     */
    public String[] getLocators();

    /**
     * add a unicast url
     *
     * @param url in the form of jini://<ip>:<port>
     * @throws MalformedURLException
     */
    public void addLocator(String url) throws MalformedURLException;

    /**
     * remove a unicast url
     *
     * @param url in the form of jini://<ip>:<port>
     * @throws MalformedURLException
     */
    public void removeLocator(String url) throws MalformedURLException;

    /**
     * sets an array of Jini unicast urls
     * @param urls in the form of jini://<ip>:<port>
     * @throws MalformedURLException
     */
    void setLocators(String[] urls) throws MalformedURLException;
}

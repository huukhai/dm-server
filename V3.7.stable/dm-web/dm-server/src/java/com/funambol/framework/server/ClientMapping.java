/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2011 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.framework.server;


import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.funambol.framework.server.error.MappingException;
import com.funambol.framework.security.Sync4jPrincipal;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Represent the mapping between the LUID from the client Items and
 * the GUID from the server Items.
 *
 * @version $Id: ClientMapping.java,v 1.2 2006/08/07 21:09:22 nichele Exp $
 */
public class ClientMapping {

    //Contain LUID -> GUID for each client
    private HashMap<String, String> clientMapping = new HashMap<String, String>();

    //Contain the principal of the client device
    private Sync4jPrincipal principal = null;

    // the requested database
    private String dbURI;

    //When the Client Mapping are modified or added
    private boolean modified = false;
    private ArrayList<String> modifiedKeys = new ArrayList<String>();

    //When entries in the Mapping are deleted
    private boolean deleted = false;
    private ArrayList<String> deletedKeys = new ArrayList<String>();

    //Funambol Logging
    //Transient keyword will disable serialisation for this member
    private transient static final Logger log = Logger.getLogger(com.funambol.framework.server.ClientMapping.class.getName());

    // ------------------------------------------------------------- Contructors

    /**
     * Construct a client Mapping to a device Id the Mapping must be populate
     * with data from the persistence store by calling
     * PersistenceStoreManager.read.
     *
     */
    public ClientMapping(Sync4jPrincipal principal, String dbURI) {
        this.principal = principal;
        this.dbURI     = dbURI    ;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * @return true if the mapping where modified or added since initialization
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @return true if item where deleted since the initialisation
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Initialize the client Mapping with data from the Persistence Store
     * @param mapping A structure that contain LUID -> GUID
     */
    public void initializeFromMapping(Map<String, String> mapping) {
        resetMapping();
        clientMapping.putAll(mapping);
    }

    /**
     * Get the deleted Entries from the client Mapping
     * @return A map that contain a mapping of LUID -> GUID
     */
    public Map<String, String> getDeletedEntries() {
        HashMap<String, String> result = null;
        if (deleted) {
            result = new HashMap<String, String>();
            for (int i = 0; i < deletedKeys.size(); i++) {
                String item = deletedKeys.get(i);
                result.put(item, clientMapping.get(item));
            }
        }
        return result;
    }

    /**
     * Get the modified entries from the client Mapping
     * @return A map with only the modified entries LUID -> GUID
     */
    public Map<String, String> getModifiedEntries() {
        HashMap<String, String> result = null;
        if (modified) {
            result = new HashMap<String, String>();
            for (int i = 0; i < modifiedKeys.size(); i++) {
                String item = modifiedKeys.get(i);
                result.put(item, clientMapping.get(item));
            }
        }
        return result;
    }

    /**
     * Get the current mapping.
     *
     * @return the current mapping as a Map
     */
    public Map<String, String> getMapping() {
        Map<String, String> ret = new HashMap<String, String>();

        ret.putAll(clientMapping);

        return ret;
    }

    /**
     * Get the Client Device Id that correspond to that Client
     * @return The Device Id
     */
    public String getClientDeviceId() {
        return principal.getDeviceId();
    }

    /**
     * Get the principal
     *
     * @return the principal
     */
    public Sync4jPrincipal getPrincipal() {
        return principal;
    }

    /**
     * Get the database uri
     *
     * @return <i>dbURI</i>
     */
    public String getDbURI() {
        return dbURI;
    }

    /**
     * Add a mapping between a LUID and a GUID from the server
     * @param luid The LUID of the client item
     * @param guid The GUID of the server item
     * @throws MappingException
     */
    private void addMapping(String luid, String guid)
    throws MappingException {
        if (clientMapping.containsKey(guid)) {
            throw new MappingException(
            "Mapping already exists for GUID " + guid + " (" + luid +")"
            );
        }

        modified = true;

        clientMapping.put(luid, guid);
        modifiedKeys.add(luid);

        //
        // If we add an entrie that was considered deleted
        // remove it from the deleted entries.
        //
        removeDeletedKey(luid);
    }

    /**
     * Get the mapped GUID for the given LUID.
     * @param luid The LUID of the client item
     * @return The mapped value for the key in input
     */
    public String getMappedValueForLuid(String luid) {
        String result = null;
        if ((result = clientMapping.get(luid)) == null) {
            if(log.isEnabledFor(Level.TRACE)){
                log.trace("No mapping found for LUID: " + luid);
            }
        }
        return result;
    }

    /**
     * Get the mapped LUID key for the given GUID value.
     * @param guid The GUID of the client item
     * @return The mapped value for the key in input
     */
    public String getMappedValueForGuid(String guid) {
        String result = null;

        if (clientMapping.containsValue(guid)) {
            java.util.Set<String> keys = clientMapping.keySet();
            java.util.Iterator<String> itKeys = keys.iterator();
            while (itKeys.hasNext()) {
                String key = itKeys.next();
                String value = clientMapping.get(key);
                if (value.equalsIgnoreCase(guid)) {
                    return key;
                }
            }
        }

        if(log.isEnabledFor(Level.TRACE)){
            log.trace("No mapping found for GUID: " + guid);
        }

        return result;
    }

    /**
     * Remove a mapped values from the cache given a LUID.
     * @param luid The LUID for the item from the client
     */
    public void removeMappedValuesForLuid(String luid) {
        if (clientMapping.remove(luid) == null) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Client Mapping not found for LUID " + luid + " when removing");
            }

            return;
        }
        addDeletedKey(luid);
    }

    /**
     * Remove a mapped values from the cache given a GUID
     * @param guid The GUID for the item from the client
     */
    public void removeMappedValuesForGuid(String guid) {
        String luid = getMappedValueForGuid(guid);
        if (luid == null) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Client Mapping not found for GUID " + guid + " when removing");
            }

            return;
        } else {
            clientMapping.remove(luid);
        }
        addDeletedKey(luid);
    }

    /**
     * Updates a key value mapping. If the mapping does not exist, it calls
     * <code>addMapping(luid, guid)</code>, otherwise the existing mapping is
     * updated.
     *
     * @param luid The LUID value from the client item
     * @param guid The GUID value from the server item
     *
     * @throws MappingException
     */
    public void updateMapping(String luid, String guid)
    throws MappingException {
        //
        // If new, just call addMapping
        //
        if (clientMapping.containsKey(guid) == false) {
            addMapping(luid, guid);
            return;
        }

        clientMapping.remove(guid);
        clientMapping.put(luid, guid);
        deletedKeys.remove(luid);
        modifiedKeys.add(luid);
        modified = true;

        assert (modified == true);
    }

    /**
     * Clears these mappings moving the existing and new mappings to deleted.
     * This is used for example to re-initialize the LUID-GUID mapping for a
     * slow sync.
     */
    public void clearMappings() {

        deletedKeys.addAll(clientMapping.values());
        deletedKeys.addAll(modifiedKeys);

        clientMapping.clear();

        modified = false;
        deleted = true;
    }

    public String toString() {
         ToStringBuilder sb = new ToStringBuilder(this);

         sb.append("clientMapping", clientMapping);
         sb.append("modifiedKeys" , modifiedKeys );
         sb.append("deletedKeys"  , deletedKeys  );

         return sb.toString();
    }

    // --------------------------------------------------------- Private methods

    /**
     * Reset the content of the Client Mapping HashMap and the state
     * of the current mapping.
     */
    private void resetMapping() {
        //We clean the map
        if (!clientMapping.isEmpty()) {
            clientMapping.clear();
        }
        //If we have modified item to the map
        if (modified) {
            modifiedKeys.clear();
            modified = false;
        }
        //If we have deleted item to the map
        if (deleted) {
            deletedKeys.clear();
            deleted = false;
        }
    }

    /**
     * Remove LUID from the deleted entries
     * @param luid The LUID for the item from the client
     */
    private void removeDeletedKey(String luid) {
        if (deleted && deletedKeys.contains(luid)) {
            deletedKeys.remove(luid);
            if (deletedKeys.isEmpty()) {
                deleted = false;
            }
        }
    }

    /**
     * Remove LUID from the modified entries
     * @param luid The LUID for the item from the client
     */
    private void removeModifiedKey(String luid) {
        if (modified && modifiedKeys.contains(luid)) {
            modifiedKeys.remove(luid);
            if (modifiedKeys.isEmpty()) {
                modified = false;
            }
        }
    }

    /**
     * Add LUID into the deleted keys.
     * @param luid The LUID for the item from the client
     */
    private void addDeletedKey(String luid) {
        removeModifiedKey(luid);
        deleted = true;
        deletedKeys.add(luid);
    }
}

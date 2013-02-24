package org.kxml.wap;

/**
 * Represents a WbxmlInitialiser for DM-DDF 1.2
 *
 * @version $Id: DMDDFInitialiser.java,v 1.1.1.1 2006-11-15 11:24:55 nichele Exp $
 */

class DMDDFInitialiser extends WbxmlInitialiser {

    public DMDDFInitialiser() {
    }

    public void initialise(WbxmlParser aParser) {

        String publicIdentifier = null;
        int publicIdentifierId  = 0;
        if (aParser != null) {
            publicIdentifier   = aParser.getPublicIdentifier();
            publicIdentifierId = aParser.getPublicIdentifierId();
        }

        if (publicIdentifier != null &&
            "-//OMA//DTD-DM-DDF 1.2//EN".equals(publicIdentifier)) {
            aParser.setTagTable(0, SyncML.tagTableDMDDF);
        } else if (publicIdentifierId == WbxmlInitialiserFactory.DMDDF12_PUBLIC_ID_CODE.intValue()) {
            aParser.setTagTable(0, SyncML.tagTableDMDDF);
        }

        aParser.addPublicIDEntry(new PublicIDEntry(null,"-//OMA//DTD-DM-DDF 1.2//EN",
        "DMDDF","http://www.openmobilealliance.org/tech/DTD/OMA-TS-DM-DDF-V1_2.dtd", "syncml:dmddf1.2",0));
    }
}

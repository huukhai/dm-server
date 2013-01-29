/* kXML
 *
 * The contents of this file are subject to the Enhydra Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License
 * on the Enhydra web site ( http://www.enhydra.org/ ).
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific terms governing rights and limitations
 * under the License.
 *
 * The Initial Developer of kXML is Stefan Haustein. Copyright (C)
 * 2000, 2001 Stefan Haustein, D-46045 Oberhausen (Rhld.),
 * Germany. All Rights Reserved.
 *
 * Contributor(s): Nicola Fankhauser
 *
 * */


package org.kxml.wap;

/**
 * This class contains the SyncML public identifier and code tables for elements needed
 *  by the SyncMLParser.
 * @author Nicola Fankhauser
 * @version $Id: SyncML.java,v 1.1.1.1 2006-11-15 11:24:55 nichele Exp $
 */
public class SyncML {

    /**
     * SyncML public identifier (to be put in string table)
     *
     */
    public static final String SYNCML_DTD_10 = "-//SYNCML//DTD SyncML 1.0//EN";
    public static final String SYNCML_DTD_11 = "-//SYNCML//DTD SyncML 1.1//EN";
    public static final String SYNCML_DTD_12 = "-//SYNCML//DTD SyncML 1.2//EN";

    public static final String DEVINF_DTD_10 = "-//SYNCML//DTD DevInf 1.0//EN";
    public static final String DEVINF_DTD_11 = "-//SYNCML//DTD DevInf 1.1//EN";
    public static final String DEVINF_DTD_12 = "-//SYNCML//DTD DevInf 1.2//EN";

    public static final String DMDDF_DTD_12 = "-//OMA//DTD-DM-DDF 1.2//EN";

    public static final String NS_DEFAULT = "syncml"       ;
    public static final String NS_DEVINF  = "syncml:devinf";
    public static final String NS_DM_DDF_12 = "syncml:dmddf1.2";

    public static final String VER_10 = "1.0";
    public static final String VER_11 = "1.1";
    public static final String VER_12 = "1.2";


    public static final String[] tagTableSyncml = {
        "Add", // 05
        "Alert", // 06
        "Archive", // 07
        "Atomic", // 08
        "Chal", // 09
        "Cmd", // 0A
        "CmdID", // 0B
        "CmdRef", // 0C
        "Copy", // 0D
        "Cred", // 0E
        "Data", // 0F
        "Delete", // 10
        "Exec", // 11
        "Final", // 12
        "Get", // 13
        "Item", // 14
        "Lang", // 15
        "LocName", // 16
        "LocURI", // 17
        "Map", // 18
        "MapItem", // 19
        "Meta", // 1A
        "MsgID", // 1B
        "MsgRef", // 1C
        "NoResp", // 1D
        "NoResults", // 1E
        "Put", // 1F
        "Replace", // 20
        "RespURI", // 21
        "Results", // 22
        "Search", // 23
        "Sequence", // 24
        "SessionID", // 25
        "SftDel", // 26
        "Source", // 27
        "SourceRef", // 28
        "Status", // 29
        "Sync", // 2A
        "SyncBody", // 2B
        "SyncHdr", // 2C
        "SyncML", // 2D
        "Target", // 2E
        "TargetRef", // 2F
        null, // 30
        "VerDTD", // 31
        "VerProto", // 32
        "NumberOfChanges", // 33
        "MoreData", // 34
        "Field", // 35
        "Filter", // 36
        "Record", // 37
        "FilterType", // 38
        "SourceParent", // 39
        "TargetParent", // 3A
        "Move", // 3B
        "Correlator" // 3C
    };

    public static final String[] tagTableMetainf = {
        "Anchor", // 05
        "EMI", // 06
        "Format", // 07
        "FreeID", // 08
        "FreeMem", // 09
        "Last", // 0A
        "Mark", // 0B
        "MaxMsgSize", // 0C
        "Mem", // 0D
        "MetInf", // 0E
        "Next", // 0F
        "NextNonce", // 10
        "SharedMem", // 11
        "Size", // 12
        "Type", // 13
        "Version", // 14
        "MaxObjSize", // 15
        "FieldLevel"  // 16
    };

    public static final String[] tagTableDevInf = {
        "CTCap", // 05
        "CTType", // 06
        "DataStore", // 07
        "DataType", // 08
        "DevID", // 09
        "DevInf", // 0a
        "DevTyp", // 0b
        "DisplayName", // 0c
        "DSMem", // 0d
        "Ext", // 0e
        "FwV", // 0f
        "HwV", // 10
        "Man", // 11
        "MaxGUIDSize", // 12
        "MaxID", // 13
        "MaxMem", // 14
        "Mod", // 15
        "OEM", // 16
        "ParamName", // 17
        "PropName", // 18
        "Rx", // 19
        "Rx-Pref", // 1a
        "SharedMem", // 1b
        "Size", // 1c
        "SourceRef", // 1d
        "SwV", // 1e
        "SyncCap", // 1f
        "SyncType", // 20
        "Tx", // 21
        "Tx-Pref", // 22
        "ValEnum", // 23
        "VerCT", // 24
        "VerDTD", // 25
        "Xnam", // 26
        "Xval", // 27
        "UTC", // 28
        "SupportNumberOfChanges", // 29
        "SupportLargeObjs", // 2a
        "Property", // 2b
        "PropParam", // 2c
        "MaxOccur", // 2d
        "NoTruncate", // 2e
        null, // 2f
        "Filter-Rx", // 30
        "FilterCap", // 31
        "FilterKeyword", // 32
        "FieldLevel", // 33
        "SupportHierarchicalSync" // 34
    };

    public static final String[] tagTableDevInf12 = {
        "CTCap", // 05
        "CTType", // 06
        "DataStore", // 07
        "DataType", // 08
        "DevID", // 09
        "DevInf", // 0a
        "DevTyp", // 0b
        "DisplayName", // 0c
        "DSMem", // 0d
        "Ext", // 0e
        "FwV", // 0f
        "HwV", // 10
        "Man", // 11
        "MaxGUIDSize", // 12
        "MaxID", // 13
        "MaxMem", // 14
        "Mod", // 15
        "OEM", // 16
        "ParamName", // 17
        "PropName", // 18
        "Rx", // 19
        "Rx-Pref", // 1a
        "SharedMem", // 1b
        "MaxSize", // 1c
        "SourceRef", // 1d
        "SwV", // 1e
        "SyncCap", // 1f
        "SyncType", // 20
        "Tx", // 21
        "Tx-Pref", // 22
        "ValEnum", // 23
        "VerCT", // 24
        "VerDTD", // 25
        "Xnam", // 26
        "Xval", // 27
        "UTC", // 28
        "SupportNumberOfChanges", // 29
        "SupportLargeObjs", // 2a
        "Property", // 2b
        "PropParam", // 2c
        "MaxOccur", // 2d
        "NoTruncate", // 2e
        null, // 2f
        "Filter-Rx", // 30
        "FilterCap", // 31
        "FilterKeyword", // 32
        "FieldLevel", // 33
        "SupportHierarchicalSync" // 34
    };
    
    public static final String[] tagTableDMDDF = {
        "AccessType", // 05
        "ACL", // 06
        "Add", // 07
        "b64", // 08
        "bin", // 09
        "bool", // 0A
        "chr", // 0B
        "CaseSense", // 0C
        "CIS", // 0D
        "Copy", // 0E
        "CS", // 0F
        "date", // 10
        "DDFName", // 11
        "DefaultValue", // 12
        "Delete", // 13
        "Description", // 14
        "DFFormat", // 15
        "DFProperties",  // 16
        "DFTitle", //17
        "DFType", //18
        "Dynamic", //19
        "Exec", //1A
        "float", //1B
        "Format", //1C
        "Get", //1D
        "int", //1E
        "Man", //1F
        "MgmtTree", //20
        "MIME", //21
        "Mod", //22
        "Name", //23
        "Node", //24
        "node", //25
        "NodeName", //26
        "null", //27
        "Occurrence", //28
        "One", //29
        "OneOrMore", //2A
        "OneOrN", //2B
        "Path", //2C
        "Permanent", //2D
        "Replace", //2E
        "RTProperties", //2F
        "Scope", //30
        "Size", //31
        "time", //32
        "Title", //33
        "TStamp", //34
        "Type", //35
        "Value", //36
        "VerDTD", //37
        "VerNo", //38
        "xml", //39
        "ZeroOrMore", //3A
        "ZeroOrN", //3B
        "ZeroOrOne" //3C
    };
}

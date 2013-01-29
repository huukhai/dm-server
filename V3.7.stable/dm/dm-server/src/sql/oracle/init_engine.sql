--
-- Initialization data for the Funambol Data Management Server
--
-- @version $Id: init_engine.sql,v 1.1 2006-08-07 21:31:59 nichele Exp $
--

--
-- FNBL_ID
--
insert into fnbl_id values('device',0);

insert into fnbl_id values('principal',0);

insert into fnbl_id values('dmstate',0);

insert into fnbl_id values('mssid',0);

--
-- FNBL_ROLE
--
insert into fnbl_role values('sync_user','Sync User');

insert into fnbl_role values('sync_administrator','Sync Administrator');

insert into fnbl_role values('special_sync_admin','Special Internal Sync Administrator');


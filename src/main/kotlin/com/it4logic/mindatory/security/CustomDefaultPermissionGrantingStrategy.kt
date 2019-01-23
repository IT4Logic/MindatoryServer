package com.it4logic.mindatory.security

import org.springframework.security.acls.domain.AuditLogger
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy
import org.springframework.security.acls.model.Acl
import org.springframework.security.acls.model.Permission
import org.springframework.security.acls.model.Sid

class CustomDefaultPermissionGrantingStrategy (private var auditLogger: AuditLogger)
    : DefaultPermissionGrantingStrategy(auditLogger) {

    override fun isGranted(acl: Acl, permission: MutableList<Permission>, sids: MutableList<Sid>, administrativeMode: Boolean): Boolean {
        for (sid in sids) { if(sid == acl.owner) return true }
        return super.isGranted(acl, permission, sids, administrativeMode)
    }
}
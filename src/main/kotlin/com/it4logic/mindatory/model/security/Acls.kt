package com.it4logic.mindatory.model.security

import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "acl_sid", uniqueConstraints = [
    (UniqueConstraint(name = "acl_sid_uk_idx", columnNames = ["sid", "principal"]))
])
data class AclSid (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @Column(name = "principal", nullable = false)
    var principal: Boolean,

    @get: NotNull
    @get: Size(max = 100)
    @Column(name = "sid", nullable = false, length = 100)
    var sid: String
)

@Entity
@Table(name = "acl_class", uniqueConstraints = [
    (UniqueConstraint(name = "acl_class_uk_idx", columnNames = ["class"]))
])
data class AclClass (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @get: Size(max = 100)
    @Column(name = "class", nullable = false, length = 100)
    var className: String
)


@Entity
@Table(name = "acl_object_identity", uniqueConstraints = [
    (UniqueConstraint(name = "acl_object_identity_uk_idx", columnNames = ["object_id_class", "object_id_identity"]))
])
data class AclObjectIdentity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "object_id_class", nullable = false)
    var objectIdClass: AclClass,

    @get: NotNull
    @Column(name = "object_id_identity", nullable = false)
    var objectIdIdentity: Long,

    @ManyToOne
    @JoinColumn(name = "parent_object")
    var parentObject: AclObjectIdentity,

    @ManyToOne
    @JoinColumn(name = "owner_sid")
    var ownerSid: AclSid,

    @get: NotNull
    @Column(name = "entries_inheriting", nullable = false)
    var entriesInheriting: Boolean
)


@Entity
@Table(name = "acl_entry", uniqueConstraints = [
    (UniqueConstraint(name = "acl_entry_uk_idx", columnNames = ["acl_object_identity", "ace_order"]))
])
data class AclEntry (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "acl_object_identity", nullable = false)
    var aclObjectIdentity: AclObjectIdentity,

    @get: NotNull
    @Column(name = "ace_order", nullable = false)
    var aceOrder: Int,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "sid", nullable = false)
    var sid: AclSid,

    @get: NotNull
    @Column(name = "mask", nullable = false)
    var mask: Int,

    @get: NotNull
    @Column(name = "granting", nullable = false)
    var granting: Boolean,

    @get: NotNull
    @Column(name = "audit_success", nullable = false)
    var auditSuccess: Boolean,

    @get: NotNull
    @Column(name = "audit_failure", nullable = false)
    var auditFailure: Boolean
)
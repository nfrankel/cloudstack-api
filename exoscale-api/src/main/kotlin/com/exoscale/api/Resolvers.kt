package com.exoscale.api

import java.util.*

fun String.resolveZoneIdWith(account: String): UUID? {
    withAccount(account)(ListZones()).listzonesresponse.zone.forEach { zone ->
        zone.name?.equals(this)?.let {
            return UUID.fromString(zone.id)
        }
    }
    return null
}

fun String.resolveServiceOfferingIdWith(account: String): UUID? {
    withAccount(account)(ListServiceOfferings()).listserviceofferingsresponse.serviceoffering.forEach { offering ->
        offering.name?.equals(this)?.let {
            return UUID.fromString(offering.id)
        }
    }
    return null
}

fun String.resolveTemplateIdWith(account: String, filter: String): UUID? {
    withAccount(account)(ListTemplates(templatefilter = filter)).listtemplatesresponse.template.forEach { template ->
        template.name?.equals(this)?.let {
            return template.id
        }
    }
    return null
}
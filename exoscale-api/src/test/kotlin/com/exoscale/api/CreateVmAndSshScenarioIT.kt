package com.exoscale.api

import org.testng.annotations.Test
import strikt.api.expectThat
import strikt.assertions.isNotNull
import java.util.*

class CreateVmAndSshScenarioIT {

    private val vmName = "java01"

    private lateinit var vmId: UUID
    private lateinit var ip: String
    private var zoneId: UUID? = null
    private var templateId: UUID? = null
    private var serviceOfferingId: UUID? = null

    @Test
    fun `should resolve zone id`() {
        zoneId = "at-vie-1".resolveZoneIdWith(ACCOUNT)
        expectThat(zoneId).isNotNull()
    }

    @Test
    fun `should resolve template id`() {
        templateId = "Linux Debian 9 64-bit".resolveTemplateIdWith(ACCOUNT, "featured")
        expectThat(templateId).isNotNull()
    }

    @Test
    fun `should resolve service offering id`() {
        serviceOfferingId = "micro".resolveServiceOfferingIdWith(ACCOUNT)
        expectThat(serviceOfferingId).isNotNull()
    }

    @Test(dependsOnMethods = ["should resolve zone id"])
    fun `should delete VM if it exists`() {
        val result = withAccount(ACCOUNT)(ListVirtualMachines(zoneid = zoneId))
        result.listvirtualmachinesresponse.virtualmachine.forEach { vm ->
            println("Found existing VM with vmName ${vm.name}")
            if (vm.name?.equals(vmName) == true) {
                vm.id?.let {
                    withAccount(ACCOUNT)(DestroyVirtualMachine(it))
                    println("Destroying VM ${vm.id} as vmName matches $vmName")
                }
            }
        }
    }

    @Test(dependsOnMethods = ["should resolve zone id",
        "should resolve template id",
        "should resolve service offering id",
        "should delete VM if it exists"])
    fun `should create VM`() {
        val result = withAccount(ACCOUNT)(DeployVirtualMachine(name = vmName,
            displayname = "Java Test Box",
            zoneid = zoneId as UUID,
            templateid = templateId as UUID,
            keypair = "auto",
            serviceofferingid = serviceOfferingId as UUID))
        vmId = result.id
        ip = result.publicip
    }

    @Test(dependsOnMethods = ["should create VM"])
    fun `ssh into VM`() {

    }
}
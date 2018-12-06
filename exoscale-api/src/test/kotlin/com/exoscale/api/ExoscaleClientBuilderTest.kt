package com.exoscale.api

import org.testng.annotations.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ExoscaleClientBuilderTest {

    private lateinit var userHome: String

    @BeforeMethod
    fun setUp() {
        userHome = System.getProperty("user.home")
        val dir = javaClass.classLoader.getResource(".").file
        System.setProperty("user.home", dir)
    }

    @Test
    fun `should initialize default client with values found in the configuration file`() {
        val execute = withDefaultAccount()
        expectThat(execute) {
            get { baseUrl }.isEqualTo("https://funky.exoscale.ch/compute")
            get { apiKey }.isEqualTo("key")
            get { apiSecret }.isEqualTo("secret")
        }
    }

    @Test
    fun `should initialize specific client with values found in the configuration file`() {
        val execute = withAccount("jane.doe@exoscale.com")
        expectThat(execute) {
            get { baseUrl }.isEqualTo("https://another.exoscale.ch/compute")
            get { apiKey }.isEqualTo("another-key")
            get { apiSecret }.isEqualTo("another-secret")
        }
    }

    @AfterMethod
    fun tearDown() {
        System.setProperty("user.home", userHome)
    }
}
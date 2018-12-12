@file:JvmName("ExoscaleClientBuilder")
package com.exoscale.api

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import java.io.File

private const val DEFAULT_COMPUTE_URL = "https://api.exoscale.ch/compute"
private const val USER_HOME_PROPERTY_NAME = "user.home"
private const val EXOSCALE_CONFIG_PATH = ".exoscale/exoscale.toml"

private const val ACCOUNT_KEY = "account"
private const val ENDPOINT_KEY = "computeEndpoint"
private const val KEY_KEY = "key"
private const val SECRET_KEY = "secret"

fun withDefaultAccount(): ExoscaleClient {
    val config = readConfig()
    return createExecute(config) { config[accountConfig.defaultaccount] }
}

fun withAccount(account: String): ExoscaleClient = createExecute { account }

fun withCredentials(apiKey: String, apiSecret: String) = ExoscaleClient(DEFAULT_COMPUTE_URL, apiKey, apiSecret)

private object accountConfig : ConfigSpec() {
    val defaultaccount by required<String>()
    val accounts by required<Set<Map<String, String>>>()
}

private fun createExecute(config:Config = readConfig(), getAccount: () -> String): ExoscaleClient {
    val accounts = config[accountConfig.accounts]
    val account = getAccount()
    val accountProperties: Map<String, String> = accounts.find { it[ACCOUNT_KEY] == account }
        ?: throw IllegalStateException("Account $account was not found in Exoscale configuration file. Please check your configuration.")
    val endpoint = accountProperties[ENDPOINT_KEY]
        ?: throw IllegalStateException("'$ENDPOINT_KEY' key was not found in $account section in configuration file. Please check your configuration")
    val key = accountProperties[KEY_KEY]
        ?: throw IllegalStateException("'$KEY_KEY' key was not found in $account section in configuration file. Please check your configuration")
    val secret = accountProperties[SECRET_KEY]
        ?: throw IllegalStateException("'$SECRET_KEY' key was not found in $account section in configuration file. Please check your configuration")
    return ExoscaleClient(endpoint, key, secret)
}

private fun readConfig(): Config {
    val userHome = System.getProperty(USER_HOME_PROPERTY_NAME)
    val toml = File(userHome, EXOSCALE_CONFIG_PATH)
    return Config { addSpec(accountConfig) }
        .from.toml.file(toml)
}
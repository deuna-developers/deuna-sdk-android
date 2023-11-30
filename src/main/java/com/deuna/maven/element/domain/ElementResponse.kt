import com.deuna.maven.element.domain.ElementEvent
import org.json.JSONObject

data class ElementResponse(
    val type: ElementEvent,
    val data: Data
) {
    data class Data(
        val user: User,
        val order: Order,
        val merchant: Merchant,
        val checkoutVersion: String,
        val schemaRegistry: SchemaRegistry,
        val metadata: Metadata?
    )

    data class User(
        val id: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val phone: String,
        val isGuest: Boolean,
        val merchantId: String,
        val networkId: String?,
        val userRole: String,
        val createdAt: String,
        val notAcceptedPolicies: Any?
    )

    data class Order(
        val orderId: String,
        val transactionId: String
    )

    data class Metadata(
        val errorMessage: String,
        val errorCode: String,
    )

    data class Merchant(
        val country: String,
        val currency: String,
        val id: String,
        val latitude: Double,
        val logoUrl: String,
        val longitude: Double,
        val name: String,
        val privacyPoliciesUrl: String,
        val shortName: String,
        val stores: List<Store>,
        val termAndConditionsUrl: String,
        val useDunaSend: Boolean,
        val merchantConfig: MerchantConfig
    ) {
        data class Store(
            val address: String,
            val createdAt: String,
            val id: String,
            val isDefault: Boolean,
            val latitude: Double,
            val longitude: Double,
            val name: String,
            val updatedAt: String
        )

        data class MerchantConfig(
            val id: String,
            val merchantId: String,
            val configuration: Configuration,
            val imageUrl: String,
            val theme: Theme,
            val createdAt: String,
            val updatedAt: String
        ) {
            data class Configuration(
                val excludeBillingAddress: Boolean,
                val hidePickupTime: Boolean,
                val isColorblind: Boolean,
                val isIdentityDocumentHide: Boolean
            )

            data class Theme(
                val mainColor: String,
                val secondaryColor: String,
                val backgroundColor: String
            )
        }
    }

    data class SchemaRegistry(
        val source: String,
        val schemaId: String,
        val schema: String,
        val registryName: String
    )

    companion object {
        fun fromJson(json: JSONObject): ElementResponse {
            val data = json.getJSONObject("data")
            val user = data.getJSONObject("user").let {
                User(
                    it.getString("id"),
                    it.getString("email"),
                    it.getString("first_name"),
                    it.getString("last_name"),
                    it.getString("phone"),
                    it.getBoolean("is_guest"),
                    it.getString("merchant_id"),
                    it.optString("network_id", null),
                    it.getString("user_role"),
                    it.getString("created_at"),
                    it.opt("not_accepted_policies")
                )
            }
            val order = data.getJSONObject("order").let {
                Order(
                    it.getString("order_id"),
                    it.getString("transaction_id")
                )
            }
            val merchant = data.getJSONObject("merchant").let {
                Merchant(
                    it.getString("country"),
                    it.getString("currency"),
                    it.getString("id"),
                    it.getDouble("latitude"),
                    it.getString("logo_url"),
                    it.getDouble(                    "longitude"),
                    it.getString("name"),
                    it.getString("privacy_policies_url"),
                    it.getString("short_name"),
                    it.getJSONArray("stores").let { storesJson ->
                        val stores = mutableListOf<Merchant.Store>()
                        for (i in 0 until storesJson.length()) {
                            val storeJson = storesJson.getJSONObject(i)
                            stores.add(
                                Merchant.Store(
                                    storeJson.getString("address"),
                                    storeJson.getString("created_at"),
                                    storeJson.getString("id"),
                                    storeJson.getBoolean("is_default"),
                                    storeJson.getDouble("latitude"),
                                    storeJson.getDouble("longitude"),
                                    storeJson.getString("name"),
                                    storeJson.getString("updated_at")
                                )
                            )
                        }
                        stores
                    },
                    it.getString("term_and_conditions_url"),
                    it.getBoolean("use_duna_send"),
                    it.getJSONObject("merchant_config").let { merchantConfigJson ->
                        val configuration = merchantConfigJson.getJSONObject("configuration").let {
                            Merchant.MerchantConfig.Configuration(
                                it.getBoolean("exclude_billing_address"),
                                it.getBoolean("hide_pickup_time"),
                                it.getBoolean("is_colorblind"),
                                it.getBoolean("is_identity_document_hide")
                            )
                        }
                        val theme = merchantConfigJson.getJSONObject("theme").let {
                            Merchant.MerchantConfig.Theme(
                                it.getString("main_color"),
                                it.getString("secondary_color"),
                                it.getString("background_color")
                            )
                        }
                        Merchant.MerchantConfig(
                            merchantConfigJson.getString("id"),
                            merchantConfigJson.getString("merchant_id"),
                            configuration,
                            merchantConfigJson.getString("image_url"),
                            theme,
                            merchantConfigJson.getString("created_at"),
                            merchantConfigJson.getString("updated_at")
                        )
                    }
                )
            }
            val schemaRegistry = data.getJSONObject("schemaRegistry").let {
                SchemaRegistry(
                    it.getString("source"),
                    it.getString("schemaId"),
                    it.getString("schema"),
                    it.getString("registryName")
                )
            }

            val metadata = data.optJSONObject("metadata")?.let {
                Metadata(
                    it.getString("errorMessage"),
                    it.getString("errorCode")
                )
            }
            val dataResponse = Data(user, order, merchant, data.getString("checkoutVersion"), schemaRegistry, metadata)
            return ElementResponse(ElementEvent.valueOf(json.getString("type")), dataResponse)
        }
    }
}
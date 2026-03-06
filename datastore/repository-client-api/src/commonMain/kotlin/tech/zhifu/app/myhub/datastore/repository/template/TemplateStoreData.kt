package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreData
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

sealed class TemplateStoreData : StoreData<String> {

    data class Single(
        val template: CardTemplate,
        override val id: String = template.id
    ) : TemplateStoreData(), StoreData.Single<String>

    data class Collection(
        override val items: List<Single>,
    ) : TemplateStoreData(), StoreData.Collection<String, Single> {
        val templates: List<CardTemplate> get() = items.map { it.template }

        override fun copyWith(items: List<Single>): Collection {
            return copy(items = items)
        }

        override fun insertItems(
            strategy: InsertionStrategy,
            items: List<Single>
        ): Collection = copy(
            items = when (strategy) {
                InsertionStrategy.APPEND -> this.items + items
                InsertionStrategy.PREPEND -> items + this.items
                InsertionStrategy.REPLACE -> items
            }
        )

        companion object {
            fun fromTemplates(templates: List<CardTemplate>): Collection {
                return Collection(
                    items = templates.map { Single(it) }
                )
            }
        }
    }
}

val TemplateStoreData.templates: List<CardTemplate>
    get() = (this as? TemplateStoreData.Collection)?.templates ?: emptyList()

val TemplateStoreData.template: CardTemplate?
    get() = (this as? TemplateStoreData.Single)?.template

package net.ntworld.kotlin.validator.internal

import net.ntworld.kotlin.validator.MessageBag
import net.ntworld.kotlin.validator.PremierRule
import net.ntworld.kotlin.validator.Rule

internal open class RuleCollectionImpl<T>(
    premierRule: PremierRule,
    internal var customMessage: String? = null
) : Rule<T> {
    internal val collection: MutableList<RuleExecutor<T>> = mutableListOf()
    private var premierRule: RuleExecutor<Any> = RuleExecutor(premierRule)
    override val message: String
        get() {
            return customMessage ?: ""
        }

    override fun passes(attribute: String, value: T?): Boolean {
        val startedValid = premierRule.passes(attribute, value)
        val valid = collection.fold(true) { acc, rule ->
            rule.passes(attribute, value) && acc
        }
        return startedValid && valid
    }

    internal fun buildErrorMessages(errors: MessageBag, attribute: String, value: T?) {
        if (this.message.isNotEmpty()) {
            RuleExecutor.addMessageToBagIfNeeded(errors, this.message, attribute, value)
            return
        }

        if (!premierRule.isValid) {
            return premierRule.buildErrorMessages(errors, attribute, value)
        }
        collection.forEach {
            it.buildErrorMessages(errors, attribute, value)
        }
    }

    internal fun addRule(rule: Rule<T>): RuleCollectionImpl<T> {
        if (rule is RuleCollectionImpl<T>) {
            this.collection.addAll(rule.collection)
        } else {
            this.collection.add(RuleExecutor(rule))
        }
        return this
    }
}
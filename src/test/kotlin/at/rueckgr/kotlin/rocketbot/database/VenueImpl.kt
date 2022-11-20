package at.rueckgr.kotlin.rocketbot.database

import kotlin.reflect.KClass

class VenueImpl(
    override var id: Long,
    override var name: String?,
    override var city: String?,
    override var country: String?,
    override var capacity: Int?
): Venue {
    override val entityClass: KClass<Venue>
        get() = TODO("Not yet implemented")
    override val properties: Map<String, Any?>
        get() = TODO("Not yet implemented")

    override fun copy(): Venue {
        TODO("Not yet implemented")
    }

    override fun delete(): Int {
        TODO("Not yet implemented")
    }

    override fun discardChanges() {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun flushChanges(): Int {
        TODO("Not yet implemented")
    }

    override fun get(name: String): Any? {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        TODO("Not yet implemented")
    }

    override fun set(name: String, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        TODO("Not yet implemented")
    }
}

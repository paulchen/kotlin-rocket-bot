package at.rueckgr.kotlin.rocketbot.database

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.long
import me.liuwj.ktorm.schema.varchar

interface Venue : Entity<Venue> {
    companion object : Entity.Factory<Venue>()

    var id: Long
    var name: String?
    var city: String?
    var country: String?
    var capacity: Int?
}

object Venues : Table<Venue>("venue") {
    var id = long("id").primaryKey().bindTo { it.id }
    var name = varchar("name").bindTo { it.name }
    var city = varchar("city").bindTo { it.city }
    var country = varchar("country").bindTo { it.country }
    var capacity = int("capacity").bindTo { it.capacity }
}

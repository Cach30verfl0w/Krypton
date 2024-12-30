package de.cacheoverflow.krypton.asn1.element

import de.cacheoverflow.krypton.asn1.EnumTagClass
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@JvmInline
@Suppress("MemberVisibilityCanBePrivate")
value class ASN1Sequence private constructor(val children: MutableList<ASN1Element>) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag)
        val buffer = Buffer().also { buffer -> children.forEach { it.write(buffer) } }
        sink.writeASN1Length(buffer.size)
        sink.write(buffer, buffer.size)
    }

    companion object : ASN1ElementFactory<ASN1Sequence> {
        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 0x10
        @JvmStatic override val isConstructed: Boolean = true
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): ASN1Sequence =
            ASN1Sequence(mutableListOf<ASN1Element>().also { children ->
                while (elementData.size > 0L) {
                    val child = context.readObject(elementData)
                    children.add(child)
                }
            })
    }
}

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
class ASN1ContextSpecificElement private constructor(var tag: Byte, var element: ASN1Element) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag)
        val contentBuffer = Buffer().also { element.write(it) }
        sink.writeASN1Length(contentBuffer.size)
        sink.write(contentBuffer, contentBuffer.size)
    }

    companion object {

        @JvmStatic
        fun fromData(context: ASN1ParserContext, constructed: Boolean, tag: Byte, data: Buffer): ASN1ContextSpecificElement =
            ASN1ContextSpecificElement(tag, context.readObject(data))
    }
}
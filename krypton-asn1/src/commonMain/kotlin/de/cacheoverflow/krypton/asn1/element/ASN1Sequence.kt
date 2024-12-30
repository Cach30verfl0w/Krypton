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
value class ASN1Sequence(val children: MutableList<ASN1Element>) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag)
        Buffer().also { buffer -> children.forEach { it.write(buffer) } }.use { buffer ->
            sink.writeASN1Length(buffer.size)
            sink.write(buffer, buffer.size)
        }
    }

    companion object : ASN1ElementFactory<ASN1Sequence> {
        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 0x10
        @JvmStatic override val isConstructed: Boolean = true
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): Result<ASN1Sequence> {
            val children = mutableListOf<ASN1Element>()
            while (elementData.size > 0L) {
                val result = context.readObject(elementData)
                when {
                    result.isFailure -> return Result.failure(requireNotNull(result.exceptionOrNull()))
                    else -> children.add(result.getOrThrow())
                }
            }
            return Result.success(ASN1Sequence(children))
        }
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
        Buffer().also { element.write(it) }.use { buffer ->
            sink.writeASN1Length(buffer.size)
            sink.write(buffer, buffer.size)
        }
    }

    companion object {
        @JvmStatic
        fun fromData(context: ASN1ParserContext, tag: Byte, data: Buffer): Result<ASN1ContextSpecificElement> =
            context.readObject(data).map { ASN1ContextSpecificElement(tag, it) }
    }
}

package dev.milanlaslop.wlop

class ElementSpec(private val selectionJs : String) {
    companion object {
        fun byId(id : String) : ElementSpec {
            val escaped_id = escapeJavaScriptString(id)
            return ElementSpec("return document.getElementById($escaped_id)")
        }

        fun bySelector(selector : String, index : Int = 0) : ElementSpec {
            val escaped_selector = escapeJavaScriptString(selector)
            return ElementSpec("return document.querySelectorAll($escaped_selector)[$index]")
        }

        fun byTagName(tagName : String, index : Int = 0) : ElementSpec {
            val escaped_tagName = escapeJavaScriptString(tagName)
            return ElementSpec("return document.getElementsByTagName($escaped_tagName)[$index]")
        }

        fun byTagNameAndAttribute(tagName : String, attributeName : String, attributeValue : String, index : Int = 0) : ElementSpec {
            val escaped_tagName = escapeJavaScriptString(tagName)
            val escaped_attributeName = escapeJavaScriptString(attributeName)
            val escaped_attributeValue = escapeJavaScriptString(attributeValue)
            return ElementSpec("var counter = 0; var elements = document.getElementsByTagName($escaped_tagName); for (var i=0, max=elements.length; i < max; i++) { var c = elements[i].getAttribute($escaped_attributeName); if (c && c === $escaped_attributeValue) { if (counter === $index) { return elements[i]; } ++counter; }  }")
        }

        fun byTagNameAndClass(tagName : String, className : String, index : Int = 0) : ElementSpec {
            val escaped_tagName = escapeJavaScriptString(tagName)
            val escaped_className = escapeJavaScriptString(className)
            return ElementSpec("var counter = 0; var elements = document.getElementsByTagName($escaped_tagName); for (var i=0, max=elements.length; i < max; i++) { var c = elements[i].getAttribute('class'); if (c && c.indexOf($escaped_className) !== -1) { if (counter === $index) { return elements[i]; } ++counter; }  }")
        }

        fun byInnerText(tagName : String, text : String, index : Int = 0) : ElementSpec {
            val escaped_tagName = escapeJavaScriptString(tagName)
            val escaped_text = escapeJavaScriptString(text)
            return ElementSpec("return Array.prototype.slice.call(document.querySelectorAll($escaped_tagName)).filter(a => a.innerText.indexOf($escaped_text) != -1)[$index]")
        }


    }

    fun getJs() : String {
        return "((function() { $selectionJs })())"
    }
}
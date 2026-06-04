package com.zerodev.clen.policy

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestPolicyTest {
    private val manifest = File("src/main/AndroidManifest.xml")
    private val document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(manifest)

    @Test
    fun manifestDoesNotRequestForbiddenPermissions() {
        val permissionNames = document.getElementsByTagName("uses-permission")
            .asSequence()
            .map { node -> node.attributes.getNamedItem("android:name").nodeValue }
            .toSet()

        assertFalse("INTERNET must stay out of the MVP.", "android.permission.INTERNET" in permissionNames)
        assertFalse(
            "MANAGE_EXTERNAL_STORAGE must stay out of the MVP.",
            "android.permission.MANAGE_EXTERNAL_STORAGE" in permissionNames,
        )
    }

    @Test
    fun manifestDoesNotDeclareAccessibilityService() {
        val serviceNodes = document.getElementsByTagName("service").asSequence()
        val hasAccessibilityService = serviceNodes.any { service ->
            service.childNodes.asSequence().any { child ->
                child.nodeName == "intent-filter" &&
                    child.childNodes.asSequence().any { grandchild ->
                        grandchild.attributes
                            ?.getNamedItem("android:name")
                            ?.nodeValue == "android.accessibilityservice.AccessibilityService"
                    }
            }
        }

        assertFalse("AccessibilityService is outside the MVP policy posture.", hasAccessibilityService)
    }

    @Test
    fun cleanupForegroundServiceUsesDataSyncOnly() {
        val services = document.getElementsByTagName("service").asSequence()
        val cleanupService = services.single { service ->
            service.attributes.getNamedItem("android:name").nodeValue ==
                ".service.CleanupForegroundService"
        }

        assertEquals(
            "dataSync",
            cleanupService.attributes.getNamedItem("android:foregroundServiceType").nodeValue,
        )

        val permissionNames = document.getElementsByTagName("uses-permission")
            .asSequence()
            .map { node -> node.attributes.getNamedItem("android:name").nodeValue }
            .toSet()
        assertTrue("FGS data sync permission should be declared.", "android.permission.FOREGROUND_SERVICE_DATA_SYNC" in permissionNames)
    }

    private fun org.w3c.dom.NodeList.asSequence(): Sequence<org.w3c.dom.Node> =
        (0 until length).asSequence().map { index -> item(index) }
}

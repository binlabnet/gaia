package io.codeka.gaia.modules.bo

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class TerraformImage constructor(
        @field:NotBlank @field:Pattern(regexp = """^[\w][\w.\-\/]{0,127}$""") val repository: String,
        @field:NotBlank val tag: String) {

    fun image() = "$repository:$tag"

    companion object {
        fun defaultInstance() = TerraformImage("hashicorp/terraform", "latest")
    }

}

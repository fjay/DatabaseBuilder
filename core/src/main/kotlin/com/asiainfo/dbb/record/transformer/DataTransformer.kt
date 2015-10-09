package com.asiainfo.dbb.record.transformer

import com.asiainfo.dbb.util.Registrator

interface DataTransformer : Registrator.Applicant<String> {

    fun execute(): String?
}
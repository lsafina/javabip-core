/*
 * Copyright 2012-2016 École polytechnique fédérale de Lausanne (EPFL), Switzerland
 * Copyright 2012-2016 Crossing-Tech SA, Switzerland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Simon Bliudze, Anastasia Mavridou, Radoslaw Szymanek and Alina Zolotukhina
 */
package org.javabip.glue;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class used by XmlGenericListAdapter.
 * 
 */
class ListType<T extends List<PortBaseImpl>> {

	private List<ListElementType<T>> list = new ArrayList<ListElementType<T>>();

	public ListType() {
	}

	public ListType(List<T> map) {
		for (T e : map) {
			list.add(new ListElementType<T>(e));
		}
	}

	@XmlElement(name = "option")
	public List<ListElementType<T>> getList() {
		return list;
	}

	public void setList(List<ListElementType<T>> list) {
		this.list = list;
	}
}

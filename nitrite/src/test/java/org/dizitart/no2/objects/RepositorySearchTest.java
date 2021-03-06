/*
 * Copyright 2017 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.objects;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.RecordIterable;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.data.Employee;
import org.dizitart.no2.objects.data.SubEmployee;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.dizitart.no2.FindOptions.sort;
import static org.dizitart.no2.objects.filters.ObjectFilters.ALL;
import static org.dizitart.no2.objects.filters.ObjectFilters.*;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class RepositorySearchTest extends BaseObjectRepositoryTest {
    @Test
    public void testFindWithOptions() {
        Cursor<Employee> cursor = employeeRepository.find(FindOptions.limit(0, 1));
        assertEquals(cursor.size(), 1);
        assertNotNull(cursor.firstOrDefault());
    }

    @Test
    public void testEmployeeProjection() {
        List<Employee> employeeList = employeeRepository.find().toList();
        List<SubEmployee> subEmployeeList
                = employeeRepository.find().project(SubEmployee.class).toList();

        assertNotNull(employeeList);
        assertNotNull(subEmployeeList);

        assertTrue(employeeList.size() > 0);
        assertTrue(subEmployeeList.size() > 0);

        assertEquals(employeeList.size(), subEmployeeList.size());

        for (int i = 0; i < subEmployeeList.size(); i++) {
            Employee employee = employeeList.get(i);
            SubEmployee subEmployee = subEmployeeList.get(i);

            assertEquals(employee.getEmpId(), subEmployee.getEmpId());
            assertEquals(employee.getJoinDate(), subEmployee.getJoinDate());
            assertEquals(employee.getAddress(), subEmployee.getAddress());
        }

        Cursor<Employee> cursor = employeeRepository.find();
        assertNotNull(cursor.firstOrDefault());
        cursor.reset();
        assertNotNull(cursor.toString());
        assertEquals(cursor.toList().size(), employeeList.size());
        assertNotNull(cursor.firstOrDefault());
        assertEquals(cursor.toList().size(), employeeList.size());
    }

    @Test
    public void testEmptyResultProjection() {
        employeeRepository.remove(ALL);
        assertNull(employeeRepository.find().firstOrDefault());

        assertNull(employeeRepository.find(eq("empId", -1))
                .firstOrDefault());
    }

    @Test
    public void testEqualFilterById() {
        Employee employee = employeeRepository.find().firstOrDefault();
        long empId = employee.getEmpId();
        Employee emp = employeeRepository.find(eq("empId", empId))
                .project(Employee.class).firstOrDefault();
        assertEquals(employee, emp);
    }

    @Test
    public void testEqualFilter() {
        Employee employee = employeeRepository.find()
                .firstOrDefault();

        Employee emp = employeeRepository.find(eq("joinDate", employee.getJoinDate()))
                .project(Employee.class)
                .firstOrDefault();
        assertEquals(employee, emp);
    }

    @Test
    public void testAndFilter() {
        Employee emp = employeeRepository.find().firstOrDefault();

        long id = emp.getEmpId();
        String address = emp.getAddress();
        Date joinDate = emp.getJoinDate();

        Employee employee = employeeRepository.find(and(
                eq("empId", id),
                regex("address", address),
                eq("joinDate", joinDate))).firstOrDefault();

        assertEquals(emp, employee);
    }

    @Test
    public void testOrFilter() {
        Employee emp = employeeRepository.find().firstOrDefault();
        long id = emp.getEmpId();

        Employee employee = employeeRepository.find(
                or(
                    eq("empId", id),
                    regex("address", "n/a"),
                    eq("joinDate", null)
                )
        ).firstOrDefault();

        assertEquals(emp, employee);
    }

    @Test
    public void testNotFilter() {
        Employee emp = employeeRepository.find().firstOrDefault();
        long id = emp.getEmpId();

        Employee employee = employeeRepository.find(not(
                eq("empId", id))).firstOrDefault();
        assertNotEquals(emp, employee);
    }

    @Test
    public void testGreaterFilter() {
        Employee emp = employeeRepository.find(sort("empId", SortOrder.Ascending)).firstOrDefault();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(gt("empId", id))
                .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 9);
    }

    @Test
    public void testGreaterEqualFilter() {
        Employee emp = employeeRepository.find(sort("empId", SortOrder.Ascending)).firstOrDefault();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(gte("empId", id))
                .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 10);
    }

    @Test
    public void testLesserThanFilter() {
        Employee emp = employeeRepository.find(sort("empId", SortOrder.Descending)).firstOrDefault();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(lt("empId", id))
                .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 9);
    }

    @Test
    public void testLesserEqualFilter() {
        Employee emp = employeeRepository.find(sort("empId", SortOrder.Descending)).firstOrDefault();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(lte("empId", id))
                .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 10);
    }

    @Test
    public void testTextFilter() {
        Employee emp = employeeRepository.find().firstOrDefault();
        String text = emp.getEmployeeNote().getText();

        List<Employee> employeeList = employeeRepository.find(text("employeeNote.text", text))
                .toList();

        assertTrue(employeeList.contains(emp));
    }

    @Test
    public void testRegexFilter() {
        RecordIterable<Employee> employees = employeeRepository.find();
        int count = employees.toList().size();

        List<Employee> employeeList = employeeRepository.find(regex("employeeNote.text", ".*"))
                .toList();

        assertEquals(employeeList.size(), count);
    }

    @Test
    public void testInFilter() {
        Employee emp = employeeRepository.find(sort("empId", SortOrder.Descending)).firstOrDefault();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(in("empId", id, id - 1, id - 2))
                .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 3);

        employeeList = employeeRepository.find(in("empId", id - 1, id - 2)).toList();
        assertEquals(employeeList.size(), 2);
    }

    @Test
    public void testElemMatchFilter() {
        final ProductScore score1 = new ProductScore("abc", 10);
        final ProductScore score2 = new ProductScore("abc", 8);
        final ProductScore score3 = new ProductScore("abc", 7);
        final ProductScore score4 = new ProductScore("xyz", 5);
        final ProductScore score5 = new ProductScore("xyz", 7);
        final ProductScore score6 = new ProductScore("xyz", 8);

        ObjectRepository<ElemMatch> repository = db.getRepository(ElemMatch.class);
        ElemMatch e1 = new ElemMatch() {{
            setId(1);
            setStrArray(new String[]{"a", "b"});
            setProductScores(new ProductScore[]{score1, score4});
        }};
        ElemMatch e2 = new ElemMatch() {{
            setId(2);
            setStrArray(new String[]{"d", "e"});
            setProductScores(new ProductScore[]{score2, score5});
        }};
        ElemMatch e3 = new ElemMatch() {{
            setId(3);
            setStrArray(new String[]{"a", "f"});
            setProductScores(new ProductScore[]{score3, score6});
        }};

        repository.insert(e1, e2, e3);

        List<ElemMatch> elements = repository.find(elemMatch("productScores",
                and(eq("product", "xyz"), gte("score", 8)))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(elemMatch("productScores",
                not(lte("score", 8)))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(elemMatch("productScores",
                or(eq("product", "xyz"), gte("score", 8)))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(elemMatch("productScores",
                (eq("product", "xyz")))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(elemMatch("productScores",
                (gte("score", 10)))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(elemMatch("productScores",
                (gt("score", 8)))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(elemMatch("productScores",
                (lt("score", 7)))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(elemMatch("productScores",
                (lte("score", 7)))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(elemMatch("productScores",
                (in("score", 7, 8)))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(elemMatch("productScores",
                (regex("product", "xyz")))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(elemMatch("strArray",
                eq("$", "a"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(elemMatch("strArray",
                not(or(eq("$", "a"),
                        eq("$", "f"),
                        eq("$", "b"))))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(elemMatch("strArray",
                gt("$", "e"))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(elemMatch("strArray",
                gte("$", "e"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(elemMatch("strArray",
                lte("$", "b"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(elemMatch("strArray",
                lt("$", "a"))).toList();
        assertEquals(elements.size(), 0);

        elements = repository.find(elemMatch("strArray",
                in("$", "a", "f"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(elemMatch("strArray",
                regex("$", "a"))).toList();
        assertEquals(elements.size(), 2);
    }

    @Test
    public void testFilterAll() {
        ObjectRepository<ElemMatch> repository = db.getRepository(ElemMatch.class);
        Cursor<ElemMatch> cursor = repository.find(ObjectFilters.ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 0);

        repository.insert(new ElemMatch());
        cursor = repository.find(ObjectFilters.ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 1);
    }


    @Data
    private static class ElemMatch {
        private long id;
        private String[] strArray;
        private ProductScore[] productScores;
    }

    @Getter @Setter
    private static class ProductScore {
        private String product;
        private int score;

        ProductScore() {}

        ProductScore(String product, int score) {
            this.product = product;
            this.score = score;
        }
    }
}

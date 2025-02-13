package study.datajpa.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

	@Autowired MemberRepository memberRepository;
	@Autowired TeamRepository teamRepository;
	@PersistenceContext
	EntityManager em;

	@Autowired MemberQueryRepository memberQueryRepository;

	@Test
	void testMember() throws Exception {
		//given
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		//when
		Member findMember = memberRepository.findById(savedMember.getId()).get();

		//then
		assertThat(findMember.getId()).isEqualTo(savedMember.getId());
		assertThat(findMember.getUsername()).isEqualTo(savedMember.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	public void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		// 단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// findMember1.setUsername("member!!!!!");

		// 리스트 조회 검증
		List<Member> all = memberRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		//카운트 검증
		long count = memberRepository.count();
		assertThat(count).isEqualTo(2);

		//삭제 검증
		memberRepository.delete(member1);
		memberRepository.delete(member2);

		long deletedCount = memberRepository.count();
		assertThat(deletedCount).isEqualTo(0);
	}

	@Test
	public void findByUsernameAndAgeGreaterThan() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void testNamedQuery() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByUsername("AAA");
		Member findMember = result.get(0);
		assertThat(findMember).isEqualTo(m1);
	}

	@Test
	public void testQuery() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findUser("AAA", 10);
		assertThat(result.get(0)).isEqualTo(m1);
	}

	@Test
	public void findUsernameList() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<String> usernameList = memberRepository.findUsernameList();
		for (String s : usernameList) {
			System.out.println("s = " + s);
		}
	}

	@Test
	public void findMemberDto() {
		Team team = new Team("teamA");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		m1.setTeam(team);
		memberRepository.save(m1);

		List<MemberDto> usernameList = memberRepository.findMemberDto();
		for (MemberDto dto : usernameList) {
			System.out.println("dto = " + dto);
		}
	}

	@Test
	public void findByNames() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
		for (Member member : result) {
			System.out.println("member = " + member);
		}
	}

	@Test
	public void returnType() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> findMember1 = memberRepository.findListByUsername("AAA"); // 컬렉션: 값이 없어도 null이 아닌 빈 컬렉션을 반환함
		Member findMember2 = memberRepository.findMemberByUsername("AAA");	// 단건 조회: 값이 없으면 Null을 반환, 2건 이상인 경우 예외 발생
		Optional<Member> findMember3 = memberRepository.findOptionalByUsername("AAA");
		System.out.println("findMember3 = " + findMember3);
	}

	@Test
	public void paging() throws Exception {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

		//when
		Page<Member> page = memberRepository.findByAge(age, pageRequest);
		Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

		//then
		List<Member> content = page.getContent(); // 조회된 데이터
		long totalElements = page.getTotalElements();
		// for (Member member : content) {
		// 	System.out.println("member = " + member);
		// }
		// System.out.println("totalElements = " + totalElements);

		assertThat(content.size()).isEqualTo(3); 			//조회된 데이터 수
		assertThat(page.getTotalElements()).isEqualTo(5);	//전체 데이터 수
		assertThat(page.getNumber()).isEqualTo(0); 		//페이지 번호
		assertThat(page.getTotalPages()).isEqualTo(2); 	//전체 페이지 번호
		assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
		assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
	}

	@Test
	public void bulkUpdate() throws Exception {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 19));
		memberRepository.save(new Member("member3", 20));
		memberRepository.save(new Member("member4", 21));
		memberRepository.save(new Member("member5", 40));

		//when
		int resultCount = memberRepository.bulkAgePlus(20);

		List<Member> result = memberRepository.findByUsername("member5");
		Member member5 = result.get(0);
		System.out.println("member5 = " + member5);

		//then
		assertThat(resultCount).isEqualTo(3);
	}

	@Test
	public void findMemberLazy() throws Exception {
		//given
		//member1 -> teamA
		//member2 -> teamB
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);
		memberRepository.save(new Member("member1", 10, teamA));
		memberRepository.save(new Member("member2", 10, teamB));

		em.flush();
		em.clear();

		//when
		// List<Member> members = memberRepository.findAll(); // N + 1 문제
		// List<Member> members = memberRepository.findMemberFetchJoin(); // fetch join
		// List<Member> members = memberRepository.findAll(); // EntityGraph
		List<Member> members = memberRepository.findEntityGraphByUsername("member1"); // EntityGraph

		//then
		for (Member member : members) {
			System.out.println("member = " + member.getUsername());
			System.out.println("member.teamClass = " + member.getTeam().getClass()); // Proxy
			System.out.println("member.team = " + member.getTeam().getName());
		}
	}

	@Test
	public void queryHint() throws Exception {
		//given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		//when
		Member member = memberRepository.findReadOnlyByUsername("member1");
		member.setUsername("member2");

		em.flush(); //Update Query 실행X
	}

	@Test
	public void lock() throws Exception {
		//given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		//when
		List<Member> result = memberRepository.findLockByUsername("member1");
	}

	@Test
	public void callCustom() {
		List<Member> memberCustom = memberRepository.findMemberCustom();
	}

	@Test
	public void specBasic() throws Exception {
		//given
		Team teamA = new Team("teamA");
		em.persist(teamA);

		Member m1 = new Member("m1", 0, teamA);
		Member m2 = new Member("m2", 0, teamA);
		em.persist(m1);
		em.persist(m2);

		em.flush();
		em.clear();

		//when
		Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
		List<Member> result = memberRepository.findAll(spec);

		//then
		assertThat(result.size()).isEqualTo(1);
	}


	@Test
	public void queryByExample() throws Exception {
		//given
		Team teamA = new Team("teamA");
		em.persist(teamA);

		Member m1 = new Member("m1", 0, teamA);
		Member m2 = new Member("m2", 0, teamA);
		em.persist(m1);
		em.persist(m2);

		em.flush();
		em.clear();

		//when
		//Probe
		Member member = new Member("m1");

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths("age");

		Example<Member> example = Example.of(member, matcher);

		List<Member> result = memberRepository.findAll(example);

		assertThat(result.get(0).getUsername()).isEqualTo("m1");
	}

}

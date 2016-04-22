package org.sonatype.nexus.crowd.plugin;

import java.util.Set;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserSearchCriteria;

public interface NexusCrowdClient {
	boolean authenticate(UsernamePasswordToken token);
	User findUserByUsername(String username);
	Set<String> findAllUsernames();
	Set<User> findUsers();
	Set<User> findUserByCriteria(UserSearchCriteria criteria);

	Set<Role> findRoles();
	Role findRoleByRoleId(String roleId);
	Set<String> findRolesByUser(String username);

}

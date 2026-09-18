async function fetchUsers() {
    const users = fetch('/api/users/all');
    const data = await users.then(response => response.json());
    alert(JSON.stringify(data, null, 2));
}
async function main () {
    console.log('executed main function');
    await fetchUsers();
}
main();
# because live is to short to setup environment manually

bootstrap instances

    tail -n +2 inv | xargs -I hostname ssh root@hostname "apt-get update && apt-get -y install python"

and run playbook

    ansible-playbook -i inv warsztat.yaml -u root

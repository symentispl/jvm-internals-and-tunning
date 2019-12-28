#!/usr/bin/env bash
set -eu

#
# this script builds virtualbox image for the training
#

##
# first check if SSH private/public key exists
ssh_private_key_file="workshops.key"
source_path="ubuntu-19.10-server-cloudimg-amd64.ova"

if [[ "$#" -gt 0 ]]; then
	while [[ "$#" -gt 0 ]]; do
		case $1 in
			--ssh-private-key-file)
				ssh_private_key_file=$2
				shift 2
				;;
			--source-path)
				source_path=$2
				shift 2
				;;
			*)
				echo "unrecognized argument $1"
				exit 1
		esac
	done
fi


if [[ ! -f "$ssh_private_key_file" ]]; then
  __usage="SSH private key $ssh_private_key_file doesn't exist, please generate keys with:
	ssh-keygen -f workshops.key
or run with build.sh --ssh-private-key-file [private-key-file]"
  echo "$__usage"
  exit 1
fi

ssh_public_key_file="${ssh_private_key_file}.pub"

if [[ ! -f "$ssh_public_key_file" ]]; then
  __usage="SSH public key $ssh_public_key_file doesn't exist, please generate keys with:
	ssh-keygen -f workshops.key
or run with build.sh --ssh-public-key-file [public-key-file]"
  echo "$__usage"
  exit 1
fi

if [[ ! -f "$source_path" ]]; then
	echo "VirtualBox image at $source_path, doesn't exist copy image or run with --source-path [source-path]"
	exit 1
fi

##
# generate iso for cloud init
temp_iso_dir=$(mktemp -d)

cat <<EOT >> "$temp_iso_dir/user-data"
#cloud-config
password: ubuntu
chpasswd: { expire: False }
ssh_pwauth: True
ssh_authorized_keys:
  - "$(cat $ssh_public_key_file)"
EOT

cat <<EOT >> "$temp_iso_dir/meta-data"
instance-id: 1
local-hostname: workshops
EOT

iso_image_output=cloud-init-data.iso
(
  cd "$temp_iso_dir" || exit 1
  genisoimage -output "$iso_image_output" -volid cidata -joliet -rock user-data meta-data
)

cloud_init_data_iso="$temp_iso_dir/$iso_image_output"

##
# run packer
PACKER_LOG=1 packer build \
  -var "ssh_private_key_file=$ssh_private_key_file" \
  -var "cloud_init_data_iso=$cloud_init_data_iso" \
  -var "source_path=$source_path" \
  template.json

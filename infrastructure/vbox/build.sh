#!/usr/bin/env bash
set -eux

#
# this script build virtualbox image for the training
#

# first check if SSH private/public key exists
ssh_private_key_file="workshops.key"
ssh_public_key_file="workshops.key.pub"

if [[ ! -f "$ssh_private_key_file" ]]; then
  #statements
  echo "SSH private key $ssh_private_key_file doesn't exist"
  exit 1
fi

if [[ ! -f "$ssh_public_key_file" ]]; then
  #statements
  echo "SSH public key $ssh_public_key_file doesn't exist"
  exit 1
fi

# generate iso
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


# run packer
PACKER_LOG=1 packer build \
  -var "ssh_private_key_file=$ssh_private_key_file" \
  -var "cloud_init_data_iso=$cloud_init_data_iso" \
  -var "source_path=/home/jarek/Pobrane/ubuntu-19.10-server-cloudimg-amd64.ova" \
  template.json
